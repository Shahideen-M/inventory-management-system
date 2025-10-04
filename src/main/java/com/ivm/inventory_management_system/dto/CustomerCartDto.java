package com.ivm.inventory_management_system.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerCartDto {
    private Long cartId;
    private String customerName;
    private String itemName;
    private BigDecimal itemPrice;
    private Integer quantity;
    private String category;
    private String status;
    private LocalDateTime expiresAt;
    private BigDecimal totalAmount;
}
