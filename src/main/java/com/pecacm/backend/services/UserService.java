package com.pecacm.backend.services;

import com.pecacm.backend.constants.Constants;
import com.pecacm.backend.constants.ErrorConstants;
import com.pecacm.backend.entities.Transaction;
import com.pecacm.backend.entities.User;
import com.pecacm.backend.entities.VerificationToken;
import com.pecacm.backend.enums.EventRole;
import com.pecacm.backend.enums.Role;
import com.pecacm.backend.exception.AcmException;
import com.pecacm.backend.model.AssignRoleRequest;
import com.pecacm.backend.repository.TransactionRepository;
import com.pecacm.backend.repository.UserRepository;
import com.pecacm.backend.repository.VerificationTokenRepository;
import com.pecacm.backend.response.UserEventDetails;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final TransactionRepository transactionRepository;

    private final VerificationTokenRepository verificationTokenRepository;

    public UserService(UserRepository userRepository, VerificationTokenRepository verificationTokenRepository, TransactionRepository transactionRepository, VerificationService verificationService) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.transactionRepository = transactionRepository;
    }

    public void addUser(User user, PasswordEncoder passwordEncoder) {
        if (userRepository.existsByEmailOrSid(user.getEmail(), user.getSid())) {
            throw new AcmException("User with given email or SID already exists", HttpStatus.BAD_REQUEST);
        }
        if (Strings.isBlank(user.getEmail()) || Strings.isBlank(user.getPassword()) || user.getSid() == null ||
                Strings.isBlank(user.getBranch())
            // TODO: 22/10/23 add required password checks to stay consistent with frontend checks
        ) {
            throw new AcmException("One or more required fields are empty", HttpStatus.BAD_REQUEST);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Integer batch = 2004 + Math.floorDiv(user.getSid(), 1000000); // hacky fix
        user.setBatch(batch);
        user.setVerified(false);
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(UUID tokenId, String username, String password, PasswordEncoder passwordEncoder) {
        Optional<User> user = userRepository.findByEmail(username);
        if (user.isEmpty()) {
            throw new AcmException("Email provided does not match any of the registered users", HttpStatus.NOT_FOUND);
        }
        Optional<VerificationToken> token = verificationTokenRepository.findById(tokenId);
        if (token.isEmpty() || token.get().getCreatedDate().isBefore(LocalDateTime.now().minusMinutes(15))){
            verificationTokenRepository.deleteById(tokenId);
            throw new AcmException("UUID token provided does not match, it might be expired", HttpStatus.NOT_FOUND);
        }
        if (token.get().getUser() != user.get()){
            throw new AcmException("UUID token provided does not belong to the user.", HttpStatus.UNAUTHORIZED);
        }
        if (password.isBlank() || password.isEmpty()) {
            throw new AcmException("password cannot be blank or empty", HttpStatus.BAD_REQUEST);
            // TODO: 22/10/23 add required password checks to stay consistent with frontend checks
        }

        userRepository.updatePasswordByEmail(passwordEncoder.encode(password), username);
        verificationTokenRepository.deleteByToken(tokenId);
    }

    @Override
    public User loadUserByUsername(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new AcmException("User with provided email does not exist", HttpStatus.NOT_FOUND)
        );
    }

    public User verifyUser(UUID tokenId) {
        VerificationToken token = verificationTokenRepository.findById(tokenId).orElseThrow(() ->
                new AcmException("Verification token not found", HttpStatus.NOT_FOUND)
        );
        LocalDateTime currentDate = LocalDateTime.now();
        boolean tokenExpired = token.getCreatedDate().isBefore(currentDate.minusDays(3));
        if (tokenExpired){
            throw new AcmException("Token Expired", HttpStatus.NOT_FOUND);
        }
        User user = token.getUser();
        user.setVerified(true);
        verificationTokenRepository.deleteById(tokenId);
        return userRepository.save(user);
    }

    @Transactional
    public String changeRole(AssignRoleRequest assignRoleRequest) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Role requesterRole = userRepository.findRoleByEmail(userEmail)
                .orElseThrow(() ->
                        new AcmException(ErrorConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND)
                );

        Role requestUserRole = userRepository.findRoleByEmail(assignRoleRequest.getEmail())
                .orElseThrow(() ->
                        new AcmException(ErrorConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND)
                );

        Boolean isNewRoleLessThanUserRole = assignRoleRequest.getNewRole().compareTo(requesterRole) < 0;
        Boolean isUserAuthorizedToChangeRole = requesterRole.equals(Role.Core) || requesterRole.equals(Role.Admin);
        Boolean isRequestUserRoleLessThanRequester = requestUserRole.compareTo(requesterRole) < 0;

        if (isNewRoleLessThanUserRole && isUserAuthorizedToChangeRole && isRequestUserRoleLessThanRequester) {
            userRepository.updateRoleByEmail(assignRoleRequest.getEmail(), assignRoleRequest.getNewRole());
            return Constants.UPDATE_SUCCESS;
        }

        throw new AcmException(ErrorConstants.USER_UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
    }

    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new AcmException(ErrorConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND)
                );
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new AcmException(ErrorConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND)
                );
    }

    public Long getRank(Integer score) {
        return userRepository.countByXpGreaterThan(score) + 1;
    }

    public List<User> getLeaderboard(Integer offset, Integer pageSize) {
        return userRepository.findAllByOrderByXpDesc(PageRequest.of(offset, pageSize)).getContent();
    }

    public User updateUser(User updatedUser, String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty() || !Objects.equals(user.get().getEmail(), email)) {
            throw new AcmException("User cannot be updated", HttpStatus.BAD_REQUEST);
        }

        User existingUser = user.get();

        //User can only change these: name, branch, dp, sid
        //TODO: Iterate over fields instead of manually setting each
        try {

            if (updatedUser.getName() != null) {
                existingUser.setName(updatedUser.getName());
            }
            if (updatedUser.getDp() != null) {
                existingUser.setDp(updatedUser.getDp());
            }
            if (updatedUser.getBranch() != null) {
                existingUser.setBranch(updatedUser.getBranch());
            }
            if (updatedUser.getSid() != null) {
                existingUser.setSid(updatedUser.getSid());
                existingUser.setBatch(2004 + Math.floorDiv(updatedUser.getSid(), 1000000));
            }

            return userRepository.save(existingUser);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw new AcmException("Please fill the details carefully.", HttpStatus.BAD_REQUEST);
        }
    }

    public List<User> getLeaderboardByBatch(Integer batch, Integer offset, Integer pageSize) {
        return userRepository.findAllByBatch(batch, PageRequest.of(offset, pageSize)).getContent();
    }

    public List<UserEventDetails> getEventsForUserWithRole(EventRole eventRole, Integer pageSize, Integer offset) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.loadUserByUsername(email);
        Page<Transaction> transactionsPage = transactionRepository.findByUserIdAndRole(user.getId(), eventRole, PageRequest.of(offset, pageSize));

        return transactionsPage.getContent().stream()
                .map(transaction -> new UserEventDetails(
                        transaction.getEvent().getId(),
                        transaction.getEvent().getTitle(),
                        eventRole,
                        transaction.getXp(),
                        transaction.getEvent().getEndDate()
                ))
                .toList();
    }

    public List<UserEventDetails> getEventsForUser(Integer pageSize, Integer offset) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.loadUserByUsername(email);
        Page<Transaction> transactionsPage = transactionRepository.findByUserId(user.getId(), PageRequest.of(offset, pageSize));

        return transactionsPage.getContent().stream()
                .map(transaction -> new UserEventDetails(
                        transaction.getEvent().getId(),
                        transaction.getEvent().getTitle(),
                        transaction.getRole(),
                        transaction.getXp(),
                        transaction.getEvent().getEndDate()
                ))
                .toList();
    }

    public List<Transaction> getUserTransactions(String email, Integer offset, Integer pageSize) {
        List<Transaction> transactions = new ArrayList<>();
        transactionRepository.findByUser_EmailOrderByDateDesc(email, PageRequest.of(offset, pageSize)).forEach(transactions::add);
        return transactions;
    }

    public List<User> getFilteredUserList(String query,boolean onlyVerified){
        return userRepository.findAllBySearchQuery(query,onlyVerified);
    }

    public List<User> getUserByRole(Role role) {
        return userRepository.findAllByDesignation(role);
    }

    public List<User> getUserByEmailIds(List<String> emails) {
        return userRepository.findByEmailIn(emails);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }
}
