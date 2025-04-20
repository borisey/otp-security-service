package com.example.otp_security_service.models;

import jakarta.persistence.*;

@Entity
@Table(name = "otp_config")
public class OtpConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "expiration_minutes", nullable = false)
    private int expirationMinutes;

    @Column(name = "code_length", nullable = false)
    private int codeLength;

    public OtpConfig() {}

    public OtpConfig(int expirationMinutes, int codeLength) {
        this.expirationMinutes = expirationMinutes;
        this.codeLength = codeLength;
    }

    public Long getId() {
        return id;
    }

    public int getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(int expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }
}