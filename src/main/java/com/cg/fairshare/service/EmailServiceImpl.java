package com.cg.fairshare.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    @Autowired private JavaMailSender mailSender;

    @Autowired
    private DebtService debtService;

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
    }

    @Override
    public void sendGroupSummaryEmail(String recipientEmail, Long groupId) {
        try {
            // Generate the Excel file as byte array
            byte[] excelFile = debtService.generateGroupSummaryExcel(groupId);

            // Create the MimeMessage for email
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            // Set the email parameters
            helper.setTo(recipientEmail);  // Recipient's email
            helper.setSubject("Group Summary Excel File");
            helper.setText("Please find the attached group summary.");

            // Add the generated Excel file as attachment
            helper.addAttachment("group_summary.xlsx", new ByteArrayResource(excelFile));

            // Send the email
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}
