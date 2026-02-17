package com.example.ServiceBooking.notification;



import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationEmailSender {

    private final JavaMailSender mailSender;

    public void send(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            throw new RuntimeException("User email not found");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}

