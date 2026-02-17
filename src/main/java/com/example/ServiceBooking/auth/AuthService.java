package com.example.ServiceBooking.auth;


import com.example.ServiceBooking.auth.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    @Value("${otp.expiry-minutes}")
    private int otpExpiryMinutes;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
    }

    public void register(RegisterRequest request) {
        log.trace("Entering register method");
        log.debug("Processing user registration");
        log.info("User registration initiated");

        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("Email already registered");
            throw new RuntimeException("Email already registered");
        }

        String otp = generateOtp();

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setStatus(Status.PENDING);
        user.setOtpHash(passwordEncoder.encode(otp));
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(otpExpiryMinutes));

        userRepository.save(user);
        emailService.sendOtp(request.getEmail(), otp);
        log.debug("User registered and OTP sent successfully");
    }

    public void resendOtp(String email) {
        log.trace("Entering resendOtp method");
        log.debug("Processing OTP resend");
        log.info("OTP resend initiated");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found");
                    return new RuntimeException("User not found");
                });

        String otp = generateOtp();
        user.setOtpHash(passwordEncoder.encode(otp));
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(otpExpiryMinutes));

        userRepository.save(user);
        emailService.sendOtp(email, otp);
        log.debug("OTP resent successfully");
    }

    public void verifyOtp(VerifyOtpRequest request) {
        log.trace("Entering verifyOtp method");
        log.debug("Processing OTP verification");
        log.info("OTP verification initiated");

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found");
                    return new RuntimeException("User not found");
                });

        if (user.getOtpHash() == null || user.getOtpExpiry() == null) {
            log.error("OTP not found");
            throw new RuntimeException("OTP not found");
        }

        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            log.warn("OTP expired");
            clearOtp(user);
            throw new RuntimeException("OTP expired");
        }

        if (!passwordEncoder.matches(request.getOtp(), user.getOtpHash())) {
            log.error("Invalid OTP");
            clearOtp(user);
            throw new RuntimeException("Invalid OTP");
        }

        user.setStatus(Status.ACTIVE);
        clearOtp(user);
        userRepository.save(user);
        log.debug("OTP verified successfully");
    }

    public void setPassword(SetPasswordRequest request) {
        log.trace("Entering setPassword method");
        log.debug("Processing set password request");
        log.info("Set password initiated");

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found");
                    return new RuntimeException("User not found");
                });

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        log.debug("Password set successfully");
    }

    public AuthResponse login(LoginRequest request) {
        log.trace("Entering login method");
        log.debug("Processing login request");
        log.info("User login initiated");

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("Invalid credentials");
                    return new RuntimeException("Invalid credentials");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("Invalid credentials");
            throw new RuntimeException("Invalid credentials");
        }

        if (user.getStatus() != Status.ACTIVE) {
            log.warn("Account not active");
            throw new RuntimeException("Account not active");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        log.debug("Login successful");
        return new AuthResponse(token, user.getRole());
    }

    private void clearOtp(User user) {
        user.setOtpHash(null);
        user.setOtpExpiry(null);
    }

    private String generateOtp() {
        return String.valueOf((int) (100000 + Math.random() * 900000));
    }
}
