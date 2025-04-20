package com.example.otp_security_service.controllers;

import com.example.otp_security_service.models.User;
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

    public UserController(UserService userService) {
        this.userService = userService;
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
    public Map<String, String> deleteCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            // Получаем пользователя из базы по username
            User user = userService.findByUsername(userDetails.getUsername());

            // Удаляем по ID
            userService.deleteById(user.getId());

            return Map.of("message", "User with ID " + user.getId() + " has been deleted successfully");
        }

        return Map.of("error", "Unable to identify authenticated user");
    }
}