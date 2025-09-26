package com.ivm.inventory_management_system.dto;

import java.math.BigDecimal;

public record ManageItemRequest(
        String name,
        BigDecimal price,
        Integer quantity,
        Integer lowStockThreshold
) {}
