package com.ivm.inventory_management_system.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OwnerItemDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String category;
    private String primaryAddress;
    private String secondaryAddress;
    private String tertiaryAddress;
    private Integer lowStockThreshold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean available;
}