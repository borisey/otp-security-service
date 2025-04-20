package com.example.otp_security_service.controllers;

import com.example.otp_security_service.models.OtpCode;
import com.example.otp_security_service.models.OtpConfig;
import com.example.otp_security_service.models.OtpStatus;
import com.example.otp_security_service.models.User;
import com.example.otp_security_service.repo.OtpConfigRepository;
import com.example.otp_security_service.services.OtpService;
import com.example.otp_security_service.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final OtpService otpService;
    private final OtpConfigRepository otpConfigRepository;

    public UserController(UserService userService, OtpService otpService, OtpConfigRepository otpConfigRepository) {
        this.userService = userService;
        this.otpService = otpService;
        this.otpConfigRepository = otpConfigRepository;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return Map.of(
                    "username", userDetails.getUsername(),
                    "authorities", userDetails.getAuthorities()
            );
        }

        return Map.of("user", principal.toString());
    }

    @PostMapping("/me/delete")
    public Map<String, String> requestDelete() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            User user = userService.findByUsername(userDetails.getUsername());

            // Получаем текущую конфигурацию OTP (единственная запись)
            OtpConfig config = otpConfigRepository.findTopByOrderByIdAsc();

            if (config == null) {
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

            return Map.of(
                    "message", "OTP code has been generated and sent",
                    "otpCode", code,
                    "expiresAt", expiresAt.toString()
            );
        }

        return Map.of("error", "Unable to identify user");
    }
}