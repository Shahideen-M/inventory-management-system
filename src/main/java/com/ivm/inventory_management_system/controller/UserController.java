package com.ivm.inventory_management_system.controller;

import com.ivm.inventory_management_system.enums.BusinessType;
import com.ivm.inventory_management_system.entity.Item;
import com.ivm.inventory_management_system.entity.User;
import com.ivm.inventory_management_system.service.ItemService;
import com.ivm.inventory_management_system.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final ItemService itemService;

    public UserController(UserService userService, ItemService itemService) {
        this.userService = userService;
        this.itemService = itemService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try{
            User saved = userService.registerUser(user);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        return userService.findUserByName(user.getUsername())
                .filter(u -> u.getPassword().equals(user.getPassword()))
                .map(u -> ResponseEntity.ok("Login successful!"))
                .orElse(ResponseEntity.status(401).body("Invalid credentials"));
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateUser(@RequestBody User user) {
        return userService.findUserByName(user.getUsername())
                .map(existingUser -> {
                    existingUser.setPassword(user.getPassword());
                    existingUser.setBname(user.getBname());
                    userService.updateUser(existingUser);
                    return ResponseEntity.ok(
                            "User '" + existingUser.getUsername() + "' updated successfully.");
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
