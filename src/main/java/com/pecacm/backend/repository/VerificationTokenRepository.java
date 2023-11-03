package com.pecacm.backend.repository;

import com.pecacm.backend.entities.User;
import com.pecacm.backend.entities.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    @Query("SELECT v from VerificationToken v WHERE v.user.email = :username ORDER BY v.createdDate DESC LIMIT 1")
    Optional<VerificationToken> findByUsername(String username);

    @Query("SELECT "+
            "CASE " +
            "WHEN :tokenId = (SELECT v.token from VerificationToken v WHERE v.user=:user ORDER BY v.createdDate DESC LIMIT 1)" +
            "THEN TRUE " +
            "ELSE FALSE " +
            "END as result")
    Boolean checkVerificationToken(UUID tokenId, User user);

    @Modifying
    void deleteByToken(UUID tokenId);
}
