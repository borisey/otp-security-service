package com.example.otp_security_service.repo;

import com.example.otp_security_service.models.OtpCode;
import com.example.otp_security_service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
    OtpCode findByCodeAndUser(String code, User user);
    List<OtpCode> findAllByUser(User user);
    void deleteAllByUser(User user);
}