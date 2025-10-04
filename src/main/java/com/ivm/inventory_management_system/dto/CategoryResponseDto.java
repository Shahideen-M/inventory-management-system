package com.ivm.inventory_management_system.dto;

import lombok.Data;
import java.util.List;

@Data
public class CategoryResponseDto {
    private List<String> categories;
    private String defaultCategory;
}
