package com.example.ServiceBooking.auth;

import com.example.ServiceBooking.auth.dto.CreateUserRequest;
import com.example.ServiceBooking.auth.dto.UpdateProfileRequest;
import com.example.ServiceBooking.auth.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Get logged-in user
    public UserResponse getMe() {
        log.trace("Entering getMe method");
        log.debug("Processing get current user");
        log.info("Get current user initiated");

        Long userId = JwtUtil.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found");
                    return new RuntimeException("User not found");
                });

        log.debug("Current user retrieved successfully");
        return map(user);
    }

    // Update profile
    public UserResponse updateMe(UpdateProfileRequest request) {
        log.trace("Entering updateMe method");
        log.debug("Processing update user profile");
        log.info("Update user profile initiated");

        Long userId = JwtUtil.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found");
                    return new RuntimeException("User not found");
                });

        user.setName(request.name());
        if (request.city() != null && !request.city().isBlank()) {
            user.setCity(request.city().trim());
        }
        userRepository.save(user);

        log.debug("User profile updated successfully");
        return map(user);
    }

//    //  Admin creates user (NO OTP)
//    public void createUser(CreateUserRequest request) {
//        log.trace("Entering createUser method");
//        log.debug("Processing create user");
//        log.info("Create user initiated");
//
//        if (userRepository.existsByEmail(request.email())) {
//            log.error("Email already exists");
//            throw new RuntimeException("Email already exists");
//        }
//
//        User user = new User();
//        user.setName(request.name());
//        user.setEmail(request.email());
//        user.setRole(request.role());
//        user.setStatus(Status.ACTIVE); // 👈 skip OTP
//
//        userRepository.save(user);
//        log.debug("User created successfully");
//    }

    //  Admin get user by id
    public UserResponse getById(Long id) {
        log.trace("Entering getById method");
        log.debug("Processing get user by id");
        log.info("Get user by id initiated");

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found");
                    return new RuntimeException("User not found");
                });

        log.debug("User retrieved successfully");
        return map(user);
    }

    //  Soft delete
    public void deleteMe() {
        log.trace("Entering deleteMe method");
        log.debug("Processing delete user");
        log.info("Delete user initiated");

        Long userId = JwtUtil.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found");
                    return new RuntimeException("User not found");
                });

        user.setStatus(Status.DELETED);
        userRepository.save(user);
        log.debug("User deleted successfully");
    }

    private UserResponse map(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCity(),
                user.getRole(),
                user.getStatus()
        );
    }
}

