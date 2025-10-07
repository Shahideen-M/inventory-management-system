package com.ivm.inventory_management_system.dto;

import com.ivm.inventory_management_system.enums.BusinessType;
import lombok.Data;

@Data
public class UserProfileDto {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String businessName;
    private BusinessType businessType;
    private Integer lowStockThreshold;
}
