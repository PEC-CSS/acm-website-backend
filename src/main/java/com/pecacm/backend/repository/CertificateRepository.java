package com.pecacm.backend.repository;

import com.pecacm.backend.entities.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByEventId(Integer eventId);
    boolean existsByRecipientEmailAndEventId(String email, Integer eventId);
}
