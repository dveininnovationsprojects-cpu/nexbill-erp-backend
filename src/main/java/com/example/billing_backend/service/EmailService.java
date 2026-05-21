package com.example.billing_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("NEXBILL ERP - Password Reset Verification OTP");

        String mailBody = "Dear User,\n\n"
                + "A password reset request was initiated for your NEXBILL ERP account.\n\n"
                + "Your 6-digit verification code is: " + otp + "\n\n"
                + "This token is strictly valid for 10 minutes only. Do not share this code with anyone.\n\n"
                + "Regards,\n"
                + "DVein Innovations Team";

        message.setText(mailBody);

        mailSender.send(message);
    }
}