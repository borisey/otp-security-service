package com.example.otp_security_service.controllers;

import com.example.otp_security_service.models.User;
import com.example.otp_security_service.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/register")
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);  // Логгер для данного контроллера

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        logger.info("Registration request received for user: {}", user.getUsername());

        try {
            User createdUser = userService.createUser(user);

            logger.info("User successfully registered with username: {}", createdUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            logger.error("Error during user registration for username: {}", user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}