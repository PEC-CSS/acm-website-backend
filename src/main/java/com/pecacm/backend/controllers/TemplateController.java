package com.pecacm.backend.controllers;

import com.pecacm.backend.constants.Constants;
import com.pecacm.backend.entities.Event;
import com.pecacm.backend.entities.Template;
import com.pecacm.backend.repository.EventRepository;
import com.pecacm.backend.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

// templates decide what every issued certificate looks like, so they are
// restricted to the same roles that manage certificates
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@PreAuthorize(Constants.HAS_ROLE_CORE_AND_ABOVE)
@Slf4j
public class TemplateController {

    private final TemplateRepository templateRepository;
    private final EventRepository eventRepository;

    @Value("${app.upload.dir:${user.home}/acm-uploads}")
    private String uploadDir;

    @GetMapping
    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }

    @PostMapping("/upload/{eventId}")
    public ResponseEntity<Template> uploadTemplate(@PathVariable Integer eventId, @RequestParam("file") MultipartFile file) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = "template_" + eventId + "_" + System.currentTimeMillis() + ".pdf";
            Path filePath = Paths.get(uploadDir, fileName);
            Files.write(filePath, file.getBytes());

            Template template = Template.builder()
                    .templateName(file.getOriginalFilename())
                    .templatePdfPath(filePath.toString())
                    .build();

            Template savedTemplate = templateRepository.save(template);
            
            event.setTemplate(savedTemplate);
            eventRepository.save(event);

            return ResponseEntity.ok(savedTemplate);
        } catch (IOException e) {
            log.error("Failed to upload template", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
