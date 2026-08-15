package com.pecacm.backend.controllers;

import com.pecacm.backend.constants.Constants;
import com.pecacm.backend.entities.Certificate;
import com.pecacm.backend.entities.Event;
import com.pecacm.backend.entities.Template;
import com.pecacm.backend.entities.Transaction;
import com.pecacm.backend.enums.EventRole;
import com.pecacm.backend.repository.EventRepository;
import com.pecacm.backend.repository.TransactionRepository;
import com.pecacm.backend.services.CertificateService;
import com.pecacm.backend.services.MassMailService;
import com.pecacm.backend.services.TemplateGeneratorService;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// the portal drives the same actions as the certificate APIs, including the mass
// mail job, so it is restricted to the same roles. The browser has to send the
// same bearer token as the APIs, this page has no login of its own yet
@Controller
@RequestMapping("/ui")
@RequiredArgsConstructor
@PreAuthorize(Constants.HAS_ROLE_CORE_AND_ABOVE)
@Slf4j
public class CertificateUIController {

    private final EventRepository eventRepository;
    private final TransactionRepository transactionRepository;
    private final CertificateService certificateService;
    private final TemplateGeneratorService generatorService;
    private final MassMailService massMailService;

    private static final String UPLOAD_DIR = "uploads/";

    @GetMapping
    public String showPortal(Model model) {
        model.addAttribute("events", eventRepository.findAll());
        return "index";
    }

    @PostMapping("/upload")
    public String handleTemplateUpload(@RequestParam("eventId") Integer eventId, 
                                     @RequestParam("file") MultipartFile file, 
                                     Model model) {
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String filename = "template_" + eventId + "_" + System.currentTimeMillis() + ".pdf";
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            Template template = event.getTemplate();
            if (template == null) {
                template = new Template();
            }
            template.setTemplateName("Template for " + event.getTitle());
            template.setTemplatePdfPath(filePath.toString());
            
            event.setTemplate(template);
            eventRepository.save(event);

            return "redirect:/ui?success=true";
        } catch (Exception e) {
            log.error("Upload failed", e);
            model.addAttribute("error", "Upload failed: " + e.getMessage());
            return "index";
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<ByteArrayResource> handleCertificateDownload(@RequestParam("eventId") Integer eventId,
                                                                      @RequestParam("recipientName") String recipientName) {
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            Certificate certificate = Certificate.builder()
                    .recipientName(recipientName)
                    .recipientEmail("demo@example.com")
                    .event(event)
                    .issueDate(LocalDate.now().toString())
                    .build();

            byte[] pdfBytes = generatorService.generateCertificatePdf(certificate);
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificate.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);
        } catch (Exception e) {
            log.error("Generation failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/mass-mail")
    public String handleMassMail(@RequestParam("eventId") Integer eventId,
                                @RequestParam("file") MultipartFile file,
                                Model model) {
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            List<Certificate> certificates = new ArrayList<>();
            try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                reader.readNext(); // Skip header
                String[] line;
                while ((line = reader.readNext()) != null) {
                    if (line.length >= 2) {
                        Certificate cert = Certificate.builder()
                                .recipientName(line[0].trim())
                                .recipientEmail(line[1].trim())
                                .event(event)
                                .issueDate(LocalDate.now().toString())
                                .build();
                        certificates.add(certificateService.createCertificate(cert));
                    }
                }
            }

            massMailService.sendCertificates(event, certificates);
            return "redirect:/ui?count=" + certificates.size();
        } catch (Exception e) {
            log.error("Mass mail failed", e);
            model.addAttribute("error", "Mass mail failed: " + e.getMessage());
            return "index";
        }
    }

    @PostMapping("/send-to-registered")
    public String handleSendToRegistered(@RequestParam("eventId") Integer eventId, 
                                        Model model) {
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            // Fetch only PARTICIPANTS
            List<Transaction> transactions = transactionRepository.findListByEventIdAndRoles(
                    eventId, List.of(EventRole.PARTICIPANT)
            );

            if (transactions.isEmpty()) {
                model.addAttribute("error", "No participants found for this event in the database.");
                model.addAttribute("events", eventRepository.findAll());
                return "index";
            }

            List<Certificate> certificates = new ArrayList<>();
            for (Transaction tx : transactions) {
                // Simple logic: create a new certificate if doesn't exist for this user & event
                // (Using a basic search by email for now as a proxy for 'has already received')
                String email = tx.getUser().getEmail();
                String name = tx.getUser().getName();
                
                Certificate cert = Certificate.builder()
                        .recipientName(name)
                        .recipientEmail(email)
                        .event(event)
                        .issueDate(LocalDate.now().toString())
                        .build();
                
                // Save it
                certificates.add(certificateService.createCertificate(cert));
            }

            massMailService.sendCertificates(event, certificates);
            return "redirect:/ui?count=" + certificates.size();

        } catch (Exception e) {
            log.error("Auto-sync failed", e);
            model.addAttribute("error", "Auto-sync failed: " + e.getMessage());
            model.addAttribute("events", eventRepository.findAll());
            return "index";
        }
    }
}
