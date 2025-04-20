package com.example.otp_security_service.config;

import com.example.otp_security_service.models.OtpConfig;
import com.example.otp_security_service.repo.OtpConfigRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final OtpConfigRepository otpConfigRepository;

    public DataInitializer(OtpConfigRepository otpConfigRepository) {
        this.otpConfigRepository = otpConfigRepository;
    }

    @Override
    public void run(String... args) {
        if (otpConfigRepository.count() == 0) {
            OtpConfig config = new OtpConfig(5, 6); // 5 минут, код длиной 6 символов
            otpConfigRepository.save(config);
        }
    }
}