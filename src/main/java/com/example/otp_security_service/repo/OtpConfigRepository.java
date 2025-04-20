package com.example.otp_security_service.repo;

import com.example.otp_security_service.models.OtpConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpConfigRepository extends JpaRepository<OtpConfig, Long> {
    OtpConfig findTopByOrderByIdAsc();
}