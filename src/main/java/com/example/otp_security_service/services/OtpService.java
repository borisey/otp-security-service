package com.example.otp_security_service.services;

import com.example.otp_security_service.models.*;
import com.example.otp_security_service.repo.OtpCodeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;

    public OtpService(OtpCodeRepository otpCodeRepository) {
        this.otpCodeRepository = otpCodeRepository;
    }

    public OtpCode generateOtp(User user, String operation) {
        String code = String.valueOf(new Random().nextInt(900000) + 100000); // 6-значный код
        OtpCode otp = new OtpCode();
        otp.setCode(code);
        otp.setStatus(OtpStatus.ACTIVE);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5)); // todo временно хардкод
        otp.setUser(user);
        otp.setOperation(operation);

        return otpCodeRepository.save(otp);
    }

    public String generateRandomCode(int length) {
        String digits = "0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(digits.charAt(random.nextInt(digits.length())));
        }
        return sb.toString();
    }

    public void save(OtpCode otpCode) {
        otpCodeRepository.save(otpCode);
    }

    public OtpCode findOtpByCodeAndUser(String code, User user) {
        return otpCodeRepository.findByCodeAndUser(code, user);
    }

    public List<OtpCode> findAllByUser(User user) {
        return otpCodeRepository.findAllByUser(user);
    }

    public void saveAll(List<OtpCode> otpCodes) {
        otpCodeRepository.saveAll(otpCodes);
    }
}