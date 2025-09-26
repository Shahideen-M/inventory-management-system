package com.ivm.inventory_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopItemDto {
    private String itemName;
    private Long totalSold;
}
