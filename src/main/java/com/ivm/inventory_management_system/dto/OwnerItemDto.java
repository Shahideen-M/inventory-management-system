package com.ivm.inventory_management_system.dto;

import com.ivm.inventory_management_system.enums.CategoryType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

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
    private MultipartFile imageFile;
    private String imageUrl;
    private CategoryType categoryType;
    private String customCategory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean available;
}
