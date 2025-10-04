    package com.ivm.inventory_management_system.dto;

    import lombok.Data;

    import java.math.BigDecimal;
    import java.util.List;

    @Data
    public class CustomerCartResponse {
        private List<CustomerCartDto> items;
        private BigDecimal cartTotal;
    }
