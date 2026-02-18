package com.example.ServiceBooking.auth;

import com.example.ServiceBooking.auth.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Authentication")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

//    @Operation(summary = "Register a new user")
//    @PostMapping("/register")
//    public void register(@Valid @RequestBody RegisterRequest request) {
//        log.trace("Entering register method");
//        log.debug("Processing user registration request");
//        log.info("User registration requested");
//        try {
//            authService.register(request);
//            log.debug("User registration completed successfully");
//        } catch (Exception e) {
//            log.error("Error during user registration");
//            throw e;
//        }
//    }

    @Operation(summary = "Register a new customer")
    @PostMapping("/customer/register")
    public void registerCustomer(@Valid @RequestBody CustomerRegisterRequest request) {
        authService.registerCustomer(request);
    }

    @Operation(summary = "Register a new provider (step-1: account + OTP)")
    @PostMapping("/provider/register")
    public void registerProvider(@Valid @RequestBody ProviderRegisterRequest request) {
        authService.registerProvider(request);
    }

    @Operation(summary = "Resend OTP to user's email")
    @PostMapping("/send-otp")
    public void resendOtp(@Valid @RequestBody OtpRequest request) {
        log.trace("Entering resendOtp method");
        log.debug("Processing OTP resend request");
        log.info("OTP resend requested");
        try {
            authService.resendOtp(request.getEmail());
            log.debug("OTP resent successfully");
        } catch (Exception e) {
            log.error("Error resending OTP");
            throw e;
        }
    }

    @Operation(summary = "Verify OTP and activate account")
    @PostMapping("/verify-otp")
    public void verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.trace("Entering verifyOtp method");
        log.debug("Processing OTP verification request");
        log.info("OTP verification requested");
        try {
            authService.verifyOtp(request);
            log.debug("OTP verified successfully");
        } catch (Exception e) {
            log.error("Error verifying OTP");
            throw e;
        }
    }

    @Operation(summary = "Set password for the user")
    @PostMapping("/set-password")
    public void setPassword(@Valid @RequestBody SetPasswordRequest request) {
        log.trace("Entering setPassword method");
        log.debug("Processing set password request");
        log.info("Set password requested");
        try {
            authService.setPassword(request);
            log.debug("Password set successfully");
        } catch (Exception e) {
            log.error("Error setting password");
            throw e;
        }
    }

    @Operation(summary = "Login and receive JWT token")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        log.trace("Entering login method");
        log.debug("Processing login request");
        log.info("User login requested");
        try {
            AuthResponse response = authService.login(request);
            log.debug("Login successful");
            return response;
        } catch (Exception e) {
            log.error("Error during login");
            throw e;
        }
    }
}
