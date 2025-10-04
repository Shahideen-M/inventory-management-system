package com.ivm.inventory_management_system.dto;

import lombok.Data;

@Data
public class SetNewPasswordDto {
    private String token;
    private String newPassword;
}
