    package com.ivm.inventory_management_system.dto;

    import lombok.Data;

    @Data
    public class AddToCartRequest {
        private String customerName;
        private Long itemId;
        private Integer quantity;
    }
