package com.ivm.inventory_management_system.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerItemDto {
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String category;
    private String primaryAddress;
    private Boolean available;
}
