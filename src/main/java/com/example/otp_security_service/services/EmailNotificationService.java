package com.example.otp_security_service.services;

import org.springframework.stereotype.Service;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class EmailNotificationService {

    private final String username;
    private final String password;
    private final String fromEmail;
    private final String smtpHost;
    private final int smtpPort;
    private final Session session;

    public EmailNotificationService() {
        Properties config = loadConfig();
        this.username = config.getProperty("email.username");
        this.password = config.getProperty("email.password");
        this.fromEmail = config.getProperty("email.from");
        this.smtpHost = config.getProperty("mail.smtp.host");
        this.smtpPort = Integer.parseInt(config.getProperty("mail.smtp.port"));

        this.session = Session.getInstance(config, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private Properties loadConfig() {
        try {
            Properties props = new Properties();
            props.load(EmailNotificationService.class.getClassLoader()
                    .getResourceAsStream("email.properties"));
            return props;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load email configuration", e);
        }
    }

    // Проверка доступности SMTP-сервера
    public boolean isMailServerAvailable() {
        try {
            // Попытка подключиться к SMTP серверу
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            // Создаем сеанс для тестирования соединения
            Session sessionTest = Session.getInstance(props);
            Transport transport = sessionTest.getTransport("smtp");

            transport.connect(smtpHost, username, password);

            // Отправляем команду EHLO для проверки доступности сервера
            transport.close();
            return true;
        } catch (Exception e) {
            // Если возникает исключение при подключении, сервер недоступен
            return false;
        }
    }

    public void sendCode(String toEmail, String code) {
        if (!isMailServerAvailable()) {
            throw new RuntimeException("SMTP server is not available or cannot be reached.");
        }

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Ваш OTP код");
            message.setText("Ваш код подтверждения: " + code);

            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Ошибка отправки письма", e);
        }
    }
}