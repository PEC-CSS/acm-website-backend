package com.pecacm.backend.controllers;

import com.pecacm.backend.entities.Certificate;
import com.pecacm.backend.entities.Event;
import com.pecacm.backend.repository.EventRepository;
import com.pecacm.backend.services.CertificateService;
import com.pecacm.backend.services.MassMailService;
import com.pecacm.backend.services.TemplateGeneratorService;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
@Slf4j
public class CertificateController {

    private final CertificateService certificateService;
    private final MassMailService massMailService;
    private final TemplateGeneratorService generatorService;
    private final EventRepository eventRepository;

    @GetMapping
    public List<Certificate> getAllCertificates() {
        return certificateService.getAllCertificates();
    }

    @GetMapping("/{id}")
    public Certificate getCertificate(@PathVariable Long id) {
        return certificateService.getCertificateById(id);
    }

    @PostMapping
    public Certificate createCertificate(@RequestBody Certificate certificate) {
        return certificateService.createCertificate(certificate);
    }

    @PutMapping("/{id}")
    public Certificate updateCertificate(@PathVariable Long id, @RequestBody Certificate certificate) {
        return certificateService.updateCertificate(id, certificate);
    }

    @DeleteMapping("/{id}")
    public void deleteCertificate(@PathVariable Long id) {
        certificateService.deleteCertificate(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long id) {
        Certificate certificate = certificateService.getCertificateById(id);
        byte[] pdfBytes = generatorService.generateCertificatePdf(certificate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificate.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/mass-mail/{eventId}")
    public ResponseEntity<String> sendMassMail(@PathVariable Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        
        // This is a simplified fetch, you might want to fetch only certificates for this event
        // In the original tool, Certificate had an Event reference.
        // We'll filter certificates by event here.
        List<Certificate> certificates = certificateService.getAllCertificates().stream()
                .filter(c -> c.getEvent() != null && c.getEvent().getId().equals(eventId))
                .toList();

        if (certificates.isEmpty()) {
            return ResponseEntity.badRequest().body("No certificates found for this event.");
        }

        massMailService.sendCertificates(event, certificates);
        return ResponseEntity.ok("Mass mail job started for " + certificates.size() + " recipients.");
    }

    @PostMapping("/upload-csv/{eventId}")
    public ResponseEntity<String> uploadCsv(@PathVariable Integer eventId, @RequestParam("file") MultipartFile file) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        List<Certificate> certificates = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String[] nextLine;
            // Skip header: Name, Email
            reader.readNext(); 
            
            while ((nextLine = reader.readNext()) != null) {
                if (nextLine.length >= 2) {
                    Certificate cert = Certificate.builder()
                            .recipientName(nextLine[0].trim())
                            .recipientEmail(nextLine[1].trim())
                            .event(event)
                            .issueDate(LocalDate.now().toString())
                            .build();
                    certificates.add(certificateService.createCertificate(cert));
                }
            }
        } catch (IOException | CsvValidationException e) {
            log.error("CSV processing error", e);
            return ResponseEntity.internalServerError().body("Error processing CSV: " + e.getMessage());
        }

        return ResponseEntity.ok("Successfully imported " + certificates.size() + " participants.");
    }
}
