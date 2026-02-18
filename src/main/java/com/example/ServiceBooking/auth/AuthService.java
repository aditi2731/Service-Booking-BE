package com.example.ServiceBooking.auth;


import com.example.ServiceBooking.auth.dto.*;
import com.example.ServiceBooking.providermanagement.PService;
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
    private final PService providerService;

    @Value("${otp.expiry-minutes}")
    private int otpExpiryMinutes;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       JwtUtil jwtUtil, PService providerService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.providerService = providerService;
    }

    public void registerCustomer(CustomerRegisterRequest request) {
        registerBaseUser(request.getName(), request.getEmail(), request.getCity(), Role.CUSTOMER);
    }

    public void registerProvider(ProviderRegisterRequest request) {
        registerBaseUser(request.getName(), request.getEmail(), request.getCity(), Role.PROVIDER);
        // IMPORTANT: provider onboarding profile creation will happen after OTP verify
    }


    //helper methods
    private void registerBaseUser(String name, String email, String city, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        String otp = generateOtp();

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setCity(city.trim());
        user.setStatus(Status.PENDING); // OTP not verified yet
        user.setOtpHash(passwordEncoder.encode(otp));
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(otpExpiryMinutes));

        userRepository.save(user);
        emailService.sendOtp(email, otp);
    }



//    public void register(RegisterRequest request) {
//        log.trace("Entering register method");
//        log.debug("Processing user registration");
//        log.info("User registration initiated");
//
//        if (userRepository.existsByEmail(request.getEmail())) {
//            log.error("Email already registered");
//            throw new RuntimeException("Email already registered");
//        }
//
//        String otp = generateOtp();
//
//        User user = new User();
//        user.setName(request.getName());
//        user.setEmail(request.getEmail());
//        user.setRole(request.getRole());
//        user.setCity(request.getCity().trim());
//        user.setStatus(Status.PENDING);
//        user.setOtpHash(passwordEncoder.encode(otp));
//        user.setOtpExpiry(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
//
//        userRepository.save(user);
//        emailService.sendOtp(request.getEmail(), otp);
//        log.debug("User registered and OTP sent successfully");
//    }

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

//        user.setStatus(Status.ACTIVE);
        if (user.getRole() == Role.CUSTOMER) {
            user.setStatus(Status.ACTIVE);
        } else if (user.getRole() == Role.PROVIDER) {
            user.setStatus(Status.PENDING_APPROVAL); // OTP verified, but needs admin approval
            providerService.initializeProviderAfterOtp(user.getId());

        } else {
            // Admin should NOT come through auth verification flow
            throw new RuntimeException("Invalid registration flow for role: " + user.getRole());
        }

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

        if (user.getStatus() == Status.PENDING) {
            throw new RuntimeException("Verify OTP before setting password");
        }


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

        // OTP not verified yet
        if (user.getStatus() == Status.PENDING) {
            throw new RuntimeException("Verify OTP first to activate your account.");
        }

        // Hard blocked accounts
        if (user.getStatus() == Status.SUSPENDED || user.getStatus() == Status.DELETED) {
            throw new RuntimeException("Account is not allowed");
        }

        // Password check
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new RuntimeException("Password not set. Please set your password first.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("Invalid credentials");
            throw new RuntimeException("Invalid credentials");
        }

        // Role-based status rules
        if (user.getRole() == Role.CUSTOMER) {
            // Customers must be ACTIVE
            if (user.getStatus() != Status.ACTIVE) {
                throw new RuntimeException("Account not active");
            }
        } else if (user.getRole() == Role.PROVIDER) {
            // Providers can login in PENDING_APPROVAL for onboarding (docs/setup),
            // but cannot access operational endpoints until admin approves -> ACTIVE
            if (user.getStatus() != Status.ACTIVE && user.getStatus() != Status.PENDING_APPROVAL) {
                throw new RuntimeException("Account not allowed");
            }
        } else {
            // Admin login only if ACTIVE (and admins are created internally)
            if (user.getStatus() != Status.ACTIVE) {
                throw new RuntimeException("Account not active");
            }
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
