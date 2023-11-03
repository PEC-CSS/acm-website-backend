package com.pecacm.backend.services;

import com.pecacm.backend.entities.User;
import com.pecacm.backend.entities.VerificationToken;
import com.pecacm.backend.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerificationService {

    private final VerificationTokenRepository verificationTokenRepository;

    @Autowired
    public VerificationService(VerificationTokenRepository verificationTokenRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @Transactional
    public VerificationToken getVerificationToken(User user) {
        verificationTokenRepository.deleteAllByUser(user);
        return verificationTokenRepository.save(
                VerificationToken.builder().user(user).build()
        );
    }
}
