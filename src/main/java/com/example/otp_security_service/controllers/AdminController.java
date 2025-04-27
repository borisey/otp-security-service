package com.example.otp_security_service.controllers;

import com.example.otp_security_service.models.OtpConfig;
import com.example.otp_security_service.models.Role;
import com.example.otp_security_service.models.User;
import com.example.otp_security_service.repo.UserRepository;
import com.example.otp_security_service.services.OtpConfigService;
import com.example.otp_security_service.services.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);  // Логгер для контроллера

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private OtpConfigService otpConfigService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/otp-config")
    public OtpConfig updateOtpConfig(@RequestBody OtpConfig otpConfig) {
        Integer id = 1;  // Предположим, что ID конфигурации фиксирован

        logger.info("Admin request received to update OTP config with ID: {}", id);

        try {
            // Используем данные из объекта OtpConfig для обновления
            OtpConfig updatedConfig = otpConfigService.updateOtpConfig(Long.valueOf(id), otpConfig.getExpirationMinutes(), otpConfig.getCodeLength());
            logger.info("Successfully updated OTP config with ID: {}", id);
            return updatedConfig;
        } catch (Exception e) {
            logger.error("Error updating OTP config with ID: {}", id, e);
            throw new RuntimeException("Error updating OTP config", e);
        }
    }

    // Список пользователей
    @GetMapping("/users")
    public List<User> getAllNonAdminUsers() {
        logger.info("Admin request received to fetch all non-admin users.");

        try {
            List<User> users = userRepository.findByRoleNot(Role.ADMIN);
            logger.info("Successfully fetched {} non-admin users.", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Error fetching non-admin users", e);
            throw new RuntimeException("Error fetching non-admin users", e);
        }
    }
}