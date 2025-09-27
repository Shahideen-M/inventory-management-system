package com.ivm.inventory_management_system.controller;

import com.ivm.inventory_management_system.entity.JwtUtil;
import com.ivm.inventory_management_system.enums.BusinessType;
import com.ivm.inventory_management_system.entity.User;
import com.ivm.inventory_management_system.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
        try{
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User saved = userService.registerUser(user);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        return userService.findUserByEmail(user.getEmail())
                .filter(u -> passwordEncoder.matches(user.getPassword(), u.getPassword()))
                .map(u ->
                {
                    String token = jwtUtil.generateToken(u.getEmail());
                    return ResponseEntity.ok("Bearer "+ token);
                })
                .orElse(ResponseEntity.status(401).body("Invalid credentials"));
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateUser(@RequestBody User user) {
        return userService.findUserByEmail(user.getEmail())
                .map(existingUser -> {
                    if(user.getPassword() != null && !user.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
                    }
                    // Update business name if provided
                    if(user.getBusinessName() != null && !user.getBusinessName().isEmpty()) {
                        existingUser.setBusinessName(user.getBusinessName());
                    }
                    userService.updateUser(existingUser);
                    return ResponseEntity.ok(
                            "User '" + existingUser.getEmail() + "' updated successfully.");
                })
                .orElseGet(()-> ResponseEntity.notFound().build());
    }

    @GetMapping("/{ownerId}")
    public ResponseEntity<User> findById(@PathVariable Long ownerId) {
        return userService.findById(ownerId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/business-types")
    public BusinessType[] getBusinessTypes() {
        return BusinessType.values();
    }

}
