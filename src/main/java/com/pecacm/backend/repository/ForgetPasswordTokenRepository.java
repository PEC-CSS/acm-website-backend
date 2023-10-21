package com.pecacm.backend.repository;

import com.pecacm.backend.entities.ForgetPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface ForgetPasswordTokenRepository extends JpaRepository<ForgetPasswordToken, UUID> {
    ForgetPasswordToken findByToken(UUID token);
}
