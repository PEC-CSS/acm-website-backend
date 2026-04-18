package com.pecacm.backend.services;

import com.pecacm.backend.entities.Certificate;
import com.pecacm.backend.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;

    public List<Certificate> getAllCertificates() {
        return certificateRepository.findAll();
    }

    public Certificate getCertificateById(Long id) {
        return certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificate not found with id: " + id));
    }

    public Certificate createCertificate(Certificate certificate) {
        log.info("Creating new certificate for {}", certificate.getRecipientName());
        return certificateRepository.save(certificate);
    }

    public Certificate updateCertificate(Long id, Certificate certificateDetails) {
        Certificate certificate = getCertificateById(id);
        
        certificate.setRecipientName(certificateDetails.getRecipientName());
        certificate.setRecipientEmail(certificateDetails.getRecipientEmail());
        certificate.setCertificateUrl(certificateDetails.getCertificateUrl());
        certificate.setEvent(certificateDetails.getEvent());
        certificate.setIssueDate(certificateDetails.getIssueDate());
        
        return certificateRepository.save(certificate);
    }

    public void deleteCertificate(Long id) {
        Certificate certificate = getCertificateById(id);
        certificateRepository.delete(certificate);
        log.info("Deleted certificate with id: {}", id);
    }
}
