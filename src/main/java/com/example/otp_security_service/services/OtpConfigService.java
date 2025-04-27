package com.example.otp_security_service.services;

import com.example.otp_security_service.models.OtpConfig;
import com.example.otp_security_service.repo.OtpConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OtpConfigService {

    @Autowired
    private OtpConfigRepository otpConfigRepository;

    // Метод для обновления конфигурации OTP
    public OtpConfig updateOtpConfig(Long id, int expirationMinutes, int codeLength) {
        OtpConfig otpConfig = otpConfigRepository.findById(id).orElseThrow(() -> new RuntimeException("OtpConfig not found"));

        otpConfig.setExpirationMinutes(expirationMinutes);
        otpConfig.setCodeLength(codeLength);

        return otpConfigRepository.save(otpConfig);
    }
}