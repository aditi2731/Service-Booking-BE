package com.example.ServiceBooking.auth;

import com.example.ServiceBooking.auth.dto.CreateUserRequest;
import com.example.ServiceBooking.auth.dto.UpdateProfileRequest;
import com.example.ServiceBooking.auth.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@Tag(name="User Management", description = "Endpoints for managing user profiles by themselves")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Get current user profile
    @Operation(summary = "Get current user profile - CUSTOMER/PROVIDER/ADMIN whoever token is used")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'ADMIN')")
    @GetMapping("/me")
    public UserResponse me() {
        log.trace("Entering me method");
        log.debug("Processing get current user profile request");
        log.info("Get current user profile requested");
        try {
            UserResponse response = userService.getMe();
            log.debug("Current user profile retrieved successfully");
            return response;
        } catch (Exception e) {
            log.error("Error retrieving current user profile");
            throw e;
        }
    }

    // Update current user profile
    @Operation(summary = "Update current user profile - CUSTOMER/PROVIDER/ADMIN whoever token is used")
    @PreAuthorize("hasAnyRole('CUSTOMER','PROVIDER','ADMIN')")
    @PutMapping("/me")
    public UserResponse update(@RequestBody UpdateProfileRequest request) {
        log.trace("Entering update method");
        log.debug("Processing update user profile request");
        log.info("Update user profile requested");
        try {
            UserResponse response = userService.updateMe(request);
            log.debug("User profile updated successfully");
            return response;
        } catch (Exception e) {
            log.error("Error updating user profile");
            throw e;
        }
    }

//    // Create a new user (admin creates provider or customer accounts)
//    // ADMIN ONLY
//    @PreAuthorize("hasRole('ADMIN')")
//    @PostMapping
//    public void create(@RequestBody CreateUserRequest request) {
//        log.trace("Entering create method");
//        log.debug("Processing create user request");
//        log.info("Create user requested");
//        try {
//            userService.createUser(request);
//            log.debug("User created successfully");
//        } catch (Exception e) {
//            log.error("Error creating user");
//            throw e;
//        }
//    }

    // Get any user by ID
    // ADMIN ONLY
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        log.trace("Entering getById method");
        log.debug("Processing get user by id request");
        log.info("Get user by id requested");
        try {
            UserResponse response = userService.getById(id);
            log.debug("User retrieved successfully");
            return response;
        } catch (Exception e) {
            log.error("Error retrieving user");
            throw e;
        }
    }

    // Delete current user account
    @Operation(summary = "Delete current user account - CUSTOMER/PROVIDER/ADMIN whoever token is used")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'ADMIN')")
    @DeleteMapping("/me")
    public void delete() {
        log.trace("Entering delete method");
        log.debug("Processing delete user request");
        log.info("Delete user requested");
        try {
            userService.deleteMe();
            log.debug("User deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting user");
            throw e;
        }
    }
}

