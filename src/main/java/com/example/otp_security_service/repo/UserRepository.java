package com.example.otp_security_service.repo;

import com.example.otp_security_service.models.Role;
import com.example.otp_security_service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByRoleNot(Role role);
    long count();
}