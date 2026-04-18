package com.pecacm.backend.services;

import com.pecacm.backend.entities.Certificate;
import com.pecacm.backend.entities.Template;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;

@Slf4j
@Service
public class TemplateGeneratorService {

    public byte[] generateCertificatePdf(Certificate certificate) {
        if (certificate.getEvent() == null || certificate.getEvent().getTemplate() == null) {
            throw new RuntimeException("No template assigned to this event. Please upload a PDF template first.");
        }

        Template template = certificate.getEvent().getTemplate();
        String path = template.getTemplatePdfPath();

        log.info("Generating certificate by filling pre-designed PDF form: {}", path);

        if (path == null || !new File(path).exists()) {
            log.error("Template PDF file not found at path: {}", path);
            throw new RuntimeException("Template PDF file missing or not uploaded.");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (PdfReader reader = new PdfReader(path);
                 PdfWriter writer = new PdfWriter(baos);
                 PdfDocument pdf = new PdfDocument(reader, writer)) {

                PdfAcroForm form = PdfAcroForm.getAcroForm(pdf, true);
                
                if (form.getField("recipient_name") != null) {
                    form.getField("recipient_name").setValue(certificate.getRecipientName());
                }
                if (form.getField("event_name") != null) {
                    form.getField("event_name").setValue(certificate.getEvent().getTitle());
                }
                if (form.getField("issue_date") != null) {
                    form.getField("issue_date").setValue(certificate.getIssueDate());
                }

                form.flattenFields();
            }
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate certificate from PDF form", e);
            throw new RuntimeException("Internal PDF processing error", e);
        }
    }
}
