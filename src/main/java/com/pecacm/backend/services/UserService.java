package com.pecacm.backend.services;

import com.pecacm.backend.entities.User;
import com.pecacm.backend.entities.VerificationToken;
import com.pecacm.backend.exception.AcmException;
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

    private UserRepository userRepository;

    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    public UserService(UserRepository userRepository, VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    public User addUser(User user, PasswordEncoder passwordEncoder) {
        if(userRepository.existsByEmailOrSid(user.getEmail(), user.getSid())) {
            throw new AcmException("User with given email or SID already exists", HttpStatus.CONFLICT);
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
}