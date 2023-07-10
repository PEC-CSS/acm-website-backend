package com.pecacm.backend.services;

import com.pecacm.backend.entities.User;
import com.pecacm.backend.entities.VerificationToken;
import com.pecacm.backend.enums.Role;
import com.pecacm.backend.exception.AcmException;
import com.pecacm.backend.model.AssignRoleRequest;
import com.pecacm.backend.repository.UserRepository;
import com.pecacm.backend.repository.VerificationTokenRepository;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final VerificationTokenRepository verificationTokenRepository;

    @Autowired
    public UserService(UserRepository userRepository, VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    public User addUser(User user, PasswordEncoder passwordEncoder) {
        if(userRepository.existsByEmailOrSid(user.getEmail(), user.getSid())) {
            throw new AcmException("User with given email or SID already exists", HttpStatus.BAD_REQUEST);
        }
        if(Strings.isBlank(user.getEmail()) || Strings.isBlank(user.getPassword()) || user.getSid() == null ||
            Strings.isBlank(user.getBranch())
        ) {
            throw new AcmException("One or more required fields are empty", HttpStatus.BAD_REQUEST);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
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
        // TODO: check token expiration
        User user = token.getUser();
        user.setVerified(true);
        verificationTokenRepository.deleteById(tokenId);
        return userRepository.save(user);
    }

    public String changeRole(AssignRoleRequest assignRoleRequest) {
        Role requesterRole = userRepository.findRoleByEmail(assignRoleRequest.getRequesterEmail())
            .orElseThrow(() ->
                new AcmException("User with provided email does not exist", HttpStatus.NOT_FOUND)
            );

        Role requestUserRole = userRepository.findRoleByEmail(assignRoleRequest.getRequestEmail())
                .orElseThrow(() ->
                        new AcmException("User with provided email does not exist", HttpStatus.NOT_FOUND)
                );

        Boolean isNewRoleLessThanUserRole = assignRoleRequest.getNewRole().compareTo(requesterRole) < 0;
        Boolean isUserAuthorizedToChangeRole = requesterRole.equals(Role.Core) || requesterRole.equals(Role.Admin);
        Boolean isRequestUserRoleLessThanRequester = requestUserRole.compareTo(requesterRole) < 0;

        if (isNewRoleLessThanUserRole && isUserAuthorizedToChangeRole && isRequestUserRoleLessThanRequester)
        {
            userRepository.updateRoleByEmail(assignRoleRequest.getRequestEmail(), assignRoleRequest.getNewRole());
            return "Successfully Updated";
        }

        throw new AcmException("User Unauthorized");
    }
}
