package com.example.otp_security_service.controllers;

import com.example.otp_security_service.models.User;
import com.example.otp_security_service.repo.UserRepository;
import com.example.otp_security_service.services.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService;

    // Список пользователей кроме админов
    @GetMapping("/users")
    public List<User> getAllNonAdminUsers() {
        return userRepository.findByRoleNot("ADMIN");
    }
}