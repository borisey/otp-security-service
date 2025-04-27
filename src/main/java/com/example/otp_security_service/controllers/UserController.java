package com.example.otp_security_service.controllers;

import com.example.otp_security_service.models.OtpCode;
import com.example.otp_security_service.models.OtpConfig;
import com.example.otp_security_service.models.OtpStatus;
import com.example.otp_security_service.models.User;
import com.example.otp_security_service.repo.OtpConfigRepository;
import com.example.otp_security_service.services.EmailNotificationService;
import com.example.otp_security_service.services.OtpService;
import com.example.otp_security_service.services.TelegramService;
import com.example.otp_security_service.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final OtpService otpService;
    private final OtpConfigRepository otpConfigRepository;
    private final EmailNotificationService emailService;
    private final TelegramService telegramService;

    public UserController(UserService userService, OtpService otpService,
                          OtpConfigRepository otpConfigRepository,
                          EmailNotificationService emailService,
                          TelegramService telegramService
    ) {
        this.userService = userService;
        this.otpService = otpService;
        this.otpConfigRepository = otpConfigRepository;
        this.emailService = emailService;
        this.telegramService = telegramService;
    }

    private void saveOtpCodeToFile(String code, String username, LocalDateTime expiresAt) {
        String fileName = "otp_codes.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            String entry = String.format("Username: %s, OTP Code: %s, Expires At: %s\n", username, code, expiresAt);
            writer.write(entry);
        } catch (IOException e) {
            logger.error("Error writing OTP code to file", e);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            User user = userService.findByUsername(userDetails.getUsername());
            return ResponseEntity.ok(user);
        }

        logger.error("Unable to identify user");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    @PostMapping("/me/delete")
    public Map<String, String> requestDelete() {
        logger.info("Request to delete user initiated");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            User user = userService.findByUsername(userDetails.getUsername());
            logger.info("User found: {}", user.getUsername());

            // Получаем текущую конфигурацию OTP (единственная запись)
            OtpConfig config = otpConfigRepository.findTopByOrderByIdAsc();

            if (config == null) {
                logger.error("OTP configuration not found");
                return Map.of("error", "OTP configuration not found");
            }

            // Генерация кода нужной длины
            String code = otpService.generateRandomCode(config.getCodeLength());

            // Вычисление времени окончания действия кода
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(config.getExpirationMinutes());

            // Создание и сохранение записи OTP-кода
            OtpCode otp = new OtpCode();
            otp.setUser(user);
            otp.setCode(code);
            otp.setExpiresAt(expiresAt);
            otp.setStatus(OtpStatus.ACTIVE);
            otp.setOperation("DELETE_USER");

            otpService.save(otp);

            // Сохраняем OTP-код в файл
            saveOtpCodeToFile(code, userDetails.getUsername(), expiresAt);

            // Проверка доступности SMTP-сервера
            if (emailService.isMailServerAvailable()) {
                // Отправка кода по email
                emailService.sendCode(user.getEmail(), code);
                logger.info("OTP code sent to email: {}", user.getEmail());
            }

            String destination = user.getUsername();
            telegramService.sendCode(destination, code);
            logger.info("OTP code sent to telegram: {}", destination);

            return Map.of(
                    "message", "OTP code has been generated and sent",
                    "otpCode", code,
                    "expiresAt", expiresAt.toString()
            );
        }

        logger.error("Unable to identify user");
        return Map.of("error", "Unable to identify user");
    }

    @PostMapping("/me/confirm-deletion")
    public Map<String, String> confirmDeletion(@RequestBody Map<String, String> request) {
        String code = request.get("code");

        if (code == null || code.isEmpty()) {
            logger.error("OTP code is required but not provided");
            return Map.of("error", "OTP code is required");
        }

        logger.info("Confirming deletion for OTP code: {}", code);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            User user = userService.findByUsername(userDetails.getUsername());

            // Явно подгружаем user из БД (управляемый)
            User managedUser = userService.findById(user.getId());

            // Находим OTP по коду и пользователю
            OtpCode otp = otpService.findOtpByCodeAndUser(code, managedUser);

            if (otp == null) {
                logger.error("Invalid OTP code: {}", code);
                return Map.of("error", "Invalid OTP code");
            }

            if (otp.getStatus() == OtpStatus.USED) {
                logger.error("OTP code has already been used: {}", code);
                return Map.of("error", "OTP code has already been used");
            }

            if (otp.getStatus() == OtpStatus.EXPIRED || otp.getExpiresAt().isBefore(LocalDateTime.now())) {
                otp.setStatus(OtpStatus.EXPIRED);
                otp.setUser(managedUser); // важно
                otpService.save(otp);
                logger.info("OTP code expired: {}", code);
                return Map.of("error", "OTP code has expired");
            }

            // Обновляю код
            otp.setStatus(OtpStatus.USED);
            otp.setUser(managedUser);
            otpService.save(otp);

            List<OtpCode> otps = otpService.findAllByUser(managedUser);
            for (OtpCode o : otps) {
                o.setUser(null); // отключаем внешнюю связь
            }
            otpService.saveAll(otps);

            // Удаляем пользователя
            userService.deleteById(managedUser.getId());
            logger.info("User deleted: {}", managedUser.getUsername());

            return Map.of("message", "User has been deleted");
        }

        logger.error("Unable to identify user during deletion confirmation");
        return Map.of("error", "Unable to identify user");
    }
}