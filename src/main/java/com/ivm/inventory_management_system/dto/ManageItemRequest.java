package com.ivm.inventory_management_system.dto;


import com.ivm.inventory_management_system.enums.CategoryType;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public record ManageItemRequest(
        String name,
        BigDecimal price,
        Integer quantity,
        String primaryAddress,
        String secondaryAddress,
        String tertiaryAddress,
        Integer lowStockThreshold,
        MultipartFile image,
        CategoryType categoryType,
        String customCategory
) {}
