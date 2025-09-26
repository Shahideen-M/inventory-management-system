package com.ivm.inventory_management_system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ivm.inventory_management_system.enums.BusinessType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Username is required")
    private String username;
    private String bname;

    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    private String password;

    private Integer lowStockThreshold = 10;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference
    private List<Item> items = new ArrayList<>();

}
