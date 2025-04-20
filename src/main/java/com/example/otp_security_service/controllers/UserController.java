package com.example.otp_security_service.controllers;

import com.example.otp_security_service.models.OtpCode;
import com.example.otp_security_service.models.User;
import com.example.otp_security_service.services.OtpService;
import com.example.otp_security_service.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final OtpService otpService;

    public UserController(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
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
            OtpCode otp = otpService.generateOtp(user, "DELETE_USER");

            return Map.of(
                    "message", "OTP code has been generated and sent",
                    "otpCode", otp.getCode(), // в реальном проекте не отправляем в ответ!
                    "expiresAt", otp.getExpiresAt().toString()
            );
        }

        return Map.of("error", "Unable to identify user");
    }
}