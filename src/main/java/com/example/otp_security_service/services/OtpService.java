package com.example.otp_security_service.services;

import org.springframework.stereotype.Service;

@Service
public class OtpService {
    private int codeLength = 6;
    private int ttlSeconds = 300;

    public void updateConfig(int length, int ttl) {
        this.codeLength = length;
        this.ttlSeconds = ttl;
    }

    public void deleteOtpByUserId(Long userId) {
        System.out.println("OTP for user " + userId + " deleted");
    }
}