package com.ivm.inventory_management_system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ivm.inventory_management_system.enums.BusinessType;
import com.ivm.inventory_management_system.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    @NotBlank(message = "Name is required")
    private String name;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;
    private String businessName;

    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    private String password;

    private Integer lowStockThreshold = 10;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private UserRole userRole;

    private String resetToken;
    private LocalDateTime resetTokenExpiresAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference
    private List<Item> items = new ArrayList<>();

}
