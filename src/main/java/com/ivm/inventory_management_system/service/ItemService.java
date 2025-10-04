package com.ivm.inventory_management_system.service;

import com.ivm.inventory_management_system.dto.CustomerItemDto;
import com.ivm.inventory_management_system.entity.Category;
import com.ivm.inventory_management_system.enums.BusinessType;
import com.ivm.inventory_management_system.entity.Item;
import com.ivm.inventory_management_system.entity.User;
import com.ivm.inventory_management_system.enums.CategoryType;
import com.ivm.inventory_management_system.repository.CategoryRepository;
import com.ivm.inventory_management_system.repository.ItemRepository;
import com.ivm.inventory_management_system.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public Item manageItemForOwner(Long ownerId, String name,
                                   BigDecimal price, Integer quantity,
                                   String primaryAddress, String secondaryAddress,
                                   String tertiaryAddress,Integer lowStockThreshold,
                                   CategoryType categoryType, String customCategory) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        Optional<Item> existingItemOpt = itemRepository.findByUserIdAndName(ownerId, name);

        Item item;
        if (existingItemOpt.isPresent()) {
            item = existingItemOpt.get();
            item.setQuantity(item.getQuantity() + quantity);
            if (price != null) item.setPrice(price);
            if (primaryAddress != null) item.setPrimaryAddress(primaryAddress);
            if (secondaryAddress != null) item.setSecondaryAddress(secondaryAddress);
            if (tertiaryAddress != null) item.setTertiaryAddress(tertiaryAddress);
            if (lowStockThreshold != null) item.setLowStockThreshold(lowStockThreshold);
        } else {
            item = new Item();
            item.setUser(owner);
            item.setName(name);
            item.setPrice(price);
            item.setQuantity(quantity);
            item.setPrimaryAddress(primaryAddress);
            item.setSecondaryAddress(secondaryAddress);
            item.setTertiaryAddress(tertiaryAddress);
            item.setLowStockThreshold(lowStockThreshold != null ? lowStockThreshold : owner.getLowStockThreshold());
        }
        String categoryName = null;
        if (categoryType != null && categoryType != CategoryType.OTHER) {
            categoryName = categoryType.name();
        } else if (categoryType == CategoryType.OTHER && customCategory != null && !customCategory.isBlank()) {
            categoryName = customCategory.trim().toUpperCase();
        }

        if (categoryName != null) {
            Optional<Category> categoryOpt = categoryRepository.findByName(categoryName);
            Category category;
            if (categoryOpt.isPresent()) {
                category = categoryOpt.get();
            } else {
                category = new Category();
                category.setName(categoryName);
                categoryRepository.save(category);
            }
            item.setCategory(category);
        }
        return itemRepository.save(item);
    }
}
