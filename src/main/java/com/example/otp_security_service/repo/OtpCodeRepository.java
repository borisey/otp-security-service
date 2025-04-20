package com.example.otp_security_service.repo;

import com.example.otp_security_service.models.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
}