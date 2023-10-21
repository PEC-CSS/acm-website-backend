package com.pecacm.backend.services;

import com.pecacm.backend.entities.ForgetPasswordToken;
import com.pecacm.backend.entities.User;
import com.pecacm.backend.entities.VerificationToken;
import com.pecacm.backend.repository.ForgetPasswordTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ForgetPasswordService {
    private final ForgetPasswordTokenRepository forgetPasswordTokenRepository;
    @Autowired
    public ForgetPasswordService(ForgetPasswordTokenRepository forgetPasswordTokenRepository) {
        this.forgetPasswordTokenRepository = forgetPasswordTokenRepository;
    }

    @Transactional
    public ForgetPasswordToken getForgetPasswordToken(User user) {
        return forgetPasswordTokenRepository.save(
                ForgetPasswordToken.builder().user(user).build()
        );
    }

    public ForgetPasswordToken findByToken(UUID token) {
        return forgetPasswordTokenRepository.findByToken(token);
    }

    public void deleteToken(ForgetPasswordToken token) {
        forgetPasswordTokenRepository.delete(token);
    }
}
