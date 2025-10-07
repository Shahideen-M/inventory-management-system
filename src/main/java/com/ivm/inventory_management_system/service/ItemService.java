package com.ivm.inventory_management_system.service;

import com.ivm.inventory_management_system.dto.CustomerItemDto;
import com.ivm.inventory_management_system.dto.OwnerItemDto;
import com.ivm.inventory_management_system.entity.Category;
import com.ivm.inventory_management_system.enums.BusinessType;
import com.ivm.inventory_management_system.entity.Item;
import com.ivm.inventory_management_system.entity.User;
import com.ivm.inventory_management_system.enums.CategoryType;
import com.ivm.inventory_management_system.repository.CategoryRepository;
import com.ivm.inventory_management_system.repository.ItemRepository;
import com.ivm.inventory_management_system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ItemService(ItemRepository itemRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public Optional<Item> getItemByUserIdAndId(Long userId, Long id) {
        return itemRepository.findByUserIdAndId(userId, id);
    }

    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    public List<CustomerItemDto> getItemsForCustomer(Long ownerId, BusinessType businessType) {
        List<Item> items = itemRepository.findByUserIdAndUserBusinessType(ownerId, BusinessType.valueOf(businessType.toString()));

        return items.stream()
                .map(item -> mapToDto(item, businessType))
                .toList();
    }

    private CustomerItemDto mapToDto(Item item, BusinessType businessType) {
        CustomerItemDto dto = new CustomerItemDto();
        dto.setName(item.getName());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setCategory(item.getCategory() != null ? item.getCategory().getName() : null);
        if (businessType == BusinessType.SELF_SERVICE) {
            dto.setPrimaryAddress(item.getPrimaryAddress());
        }
        return dto;
    }

    public List<CustomerItemDto> searchItemsForCustomer(String keyword, BusinessType businessType) {
        List<Item> itemsByName = itemRepository.findByNameContainingIgnoreCase(keyword);
        List<Item> itemsByCategory = itemRepository.findByCategory_NameContainingIgnoreCase(keyword);

        return Stream.concat(itemsByName.stream(), itemsByCategory.stream())
                .distinct()
                .map(item -> mapToDto(item, businessType))
                .toList();
    }

    public List<Item> searchItemsForOwner(Long userId, String keyword) {
        List<Item> itemsByName = itemRepository.findByUserIdAndNameContainingIgnoreCase(userId, keyword);
        List<Item> itemsByCategory = itemRepository.findByUserIdAndCategoryNameContainingIgnoreCase(userId, keyword);

        return Stream.concat(itemsByName.stream(), itemsByCategory.stream())
                .toList();
    }

    public OwnerItemDto manageItemForOwner(Long ownerId, OwnerItemDto request) throws IOException {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        Optional<Item> existingItemOpt = itemRepository.findByUserIdAndName(ownerId, request.getName());

        Item item;
        if (existingItemOpt.isPresent()) {
            item = existingItemOpt.get();
            item.setQuantity(item.getQuantity() + (request.getQuantity() != null ? request.getQuantity() : 0));
            if (request.getPrice() != null) item.setPrice(request.getPrice());
            if (request.getPrimaryAddress() != null) item.setPrimaryAddress(request.getPrimaryAddress());
            if (request.getSecondaryAddress() != null) item.setSecondaryAddress(request.getSecondaryAddress());
            if (request.getTertiaryAddress() != null) item.setTertiaryAddress(request.getTertiaryAddress());
            if (request.getLowStockThreshold() != null) item.setLowStockThreshold(request.getLowStockThreshold());
        } else {
            item = new Item();
            item.setUser(owner);
            item.setName(request.getName());
            item.setPrice(request.getPrice());
            item.setQuantity(request.getQuantity());
            item.setPrimaryAddress(request.getPrimaryAddress());
            item.setSecondaryAddress(request.getSecondaryAddress());
            item.setTertiaryAddress(request.getTertiaryAddress());
            item.setLowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : owner.getLowStockThreshold());
        }

        MultipartFile file = request.getImageFile();
        if (file != null && !file.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            item.setImageUrl("/uploads/" + fileName);
        }

        String categoryName;
        if (request.getCategoryType() != null && request.getCategoryType() != CategoryType.OTHER) {
            categoryName = request.getCategoryType().name();
        } else if (request.getCategoryType() == CategoryType.OTHER && request.getCustomCategory() != null) {
            categoryName = request.getCustomCategory().trim().toUpperCase();
        } else {
            categoryName = null;
        }

        if (categoryName != null) {
            Optional<Category> categoryOpt = categoryRepository.findByName(categoryName);
            Category category = categoryOpt.orElseGet(() -> {
                Category newCat = new Category();
                newCat.setName(categoryName);
                return categoryRepository.save(newCat);
            });
            item.setCategory(category);
        }

        Item savedItem = itemRepository.save(item);

        OwnerItemDto dto = new OwnerItemDto();
        dto.setId(savedItem.getId());
        dto.setName(savedItem.getName());
        dto.setPrice(savedItem.getPrice());
        dto.setQuantity(savedItem.getQuantity());
        dto.setCategory(savedItem.getCategory() != null ? savedItem.getCategory().getName() : null);
        dto.setPrimaryAddress(savedItem.getPrimaryAddress());
        dto.setSecondaryAddress(savedItem.getSecondaryAddress());
        dto.setTertiaryAddress(savedItem.getTertiaryAddress());
        dto.setLowStockThreshold(savedItem.getLowStockThreshold());
        dto.setImageUrl(savedItem.getImageUrl());
        dto.setCreatedAt(savedItem.getCreatedAt());
        dto.setUpdatedAt(savedItem.getUpdatedAt());
        dto.setAvailable(savedItem.getQuantity() > 0);

        return dto;
    }
}
