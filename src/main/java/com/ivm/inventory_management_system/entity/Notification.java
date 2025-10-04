package com.ivm.inventory_management_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private boolean accepted = false;
    private boolean rejected = false;
    private boolean completed = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime expiresAt;

    private Long customerId;
    private Long ownerId;
    private String customerName;
}
