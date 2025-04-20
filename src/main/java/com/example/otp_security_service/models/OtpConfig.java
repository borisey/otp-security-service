package com.example.otp_security_service.models;

import jakarta.persistence.*;

@Entity
@Table(name = "otp_config")
public class OtpConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int codeLength;
    private int expirationMinutes;
}