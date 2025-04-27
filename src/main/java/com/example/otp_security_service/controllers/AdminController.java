package com.example.otp_security_service.controllers;

import com.example.otp_security_service.models.User;
import com.example.otp_security_service.repo.UserRepository;
import com.example.otp_security_service.services.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);  // Логгер для контроллера

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService;

    // Список пользователей кроме админов
    @GetMapping("/users")
    public List<User> getAllNonAdminUsers() {
        logger.info("Admin request received to fetch all non-admin users.");

        try {
            List<User> users = userRepository.findByRoleNot("ADMIN");
            logger.info("Successfully fetched {} non-admin users.", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Error fetching non-admin users", e);
            throw new RuntimeException("Error fetching non-admin users", e);
        }
    }
}