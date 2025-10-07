package com.ivm.inventory_management_system.controller;

import com.ivm.inventory_management_system.dto.*;
import com.ivm.inventory_management_system.entity.JwtUtil;
import com.ivm.inventory_management_system.enums.BusinessType;
import com.ivm.inventory_management_system.entity.User;
import com.ivm.inventory_management_system.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User saved = userService.registerUser(user);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return userService.findUserByEmail(loginRequest.getEmail())
                .filter(u -> passwordEncoder.matches(loginRequest.getPassword(), u.getPassword()))
                .map(u -> {
                    String token = jwtUtil.generateToken(u.getEmail());
                    return ResponseEntity.ok("Bearer " + token);
                })
                .orElse(ResponseEntity.status(401).body("Invalid email or password"));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(Principal principal) {
        return userService.findUserByEmail(principal.getName())
                .map(user -> {
                    UserProfileDto dto = new UserProfileDto();
                    dto.setId(user.getId());
                    dto.setName(user.getName());
                    dto.setBusinessName(user.getBusinessName());
                    dto.setBusinessType(user.getBusinessType());
                    dto.setLowStockThreshold(user.getLowStockThreshold());
                    return ResponseEntity.ok(dto);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateProfile(Principal principal, @RequestBody UserProfileDto dto) {
        return userService.findUserByEmail(principal.getName())
                .map(existingUser -> {
                    if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(dto.getPassword()));
                    }
                    if (dto.getBusinessName() != null && !dto.getBusinessName().isEmpty()) {
                        existingUser.setBusinessName(dto.getBusinessName());
                    }
                    if(dto.getName() != null && !dto.getName().isEmpty()) {
                        existingUser.setName(dto.getName());
                    }
                    if (dto.getBusinessType() != null) {
                        existingUser.setBusinessType(dto.getBusinessType());
                    }
                    if (dto.getLowStockThreshold() != null) {
                        existingUser.setLowStockThreshold(dto.getLowStockThreshold());
                    }
                    userService.updateUser(existingUser);
                    return ResponseEntity.ok("Profile updated successfully");
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(Principal principal,
                                                 @RequestBody PasswordChangeDto dto) {
        return userService.findUserByEmail(principal.getName())
                .map(user -> {
                    if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
                        return ResponseEntity.status(400).body("Old password is incorrect");
                    }
                    user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
                    userService.updateUser(user);
                    return ResponseEntity.ok("Password updated successfully");
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody PasswordResetRequestDto dto) {
        return userService.findUserByEmail(dto.getEmail())
                .map(user -> {
                    String token = UUID.randomUUID().toString();
                    user.setResetToken(token);
                    user.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(30));
                    userService.updateUser(user);

                    return ResponseEntity.ok("Password reset link sent to your email");
                })
                .orElseGet(() -> ResponseEntity.status(404).body("User not found"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody SetNewPasswordDto dto) {
        Optional<User> userOpt = userService.findByResetToken(dto.getToken());
        if (userOpt.isEmpty()) return ResponseEntity.status(400).body("Invalid token");

        User user = userOpt.get();
        if (user.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(400).body("Token expired");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        userService.updateUser(user);

        return ResponseEntity.ok("Password updated successfully");
    }

    @GetMapping("/business-types")
    public BusinessType[] getBusinessTypes() {
        return BusinessType.values();
    }
}
