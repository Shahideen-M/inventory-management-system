package com.ivm.inventory_management_system.service;

import com.ivm.inventory_management_system.dto.CustomerItemDto;
import com.ivm.inventory_management_system.enums.BusinessType;
import com.ivm.inventory_management_system.entity.Item;
import com.ivm.inventory_management_system.entity.User;
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

    public ItemService(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    public Optional<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }

    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    public Item createItemForUser(Long userId, Item item) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        item.setUser(user);
        return itemRepository.save(item);
    }

    public List<CustomerItemDto> getItemsForCustomer(BusinessType businessType) {
        List<Item> items = itemRepository.findAll();

        return items.stream()
                .map(item -> mapToDto(item, businessType))
                .toList();
    }

    private CustomerItemDto mapToDto(Item item, BusinessType businessType) {
        CustomerItemDto dto = new CustomerItemDto();
        dto.setName(item.getName());
        dto.setPrice(item.getPrice());
        dto.setCategory(item.getCategory() != null ? item.getCategory().getName() : null);

        if (businessType == BusinessType.SELF_SERVICE) {
            dto.setPrimaryAddress(item.getPrimaryAddress());
            dto.setAvailable(item.getQuantity() > 0);
        } else {
            dto.setQuantity(item.getQuantity());
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

    public Item manageItemForOwner(Long ownerId, String name, BigDecimal price, Integer quantity, Integer lowStockThreshold) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        Item item = itemRepository.findByUserIdAndName(ownerId, name)
                .orElseGet(Item::new);

        item.setUser(owner);
        item.setName(name);
        item.setPrice(price);
        item.setQuantity(quantity);
        item.setLowStockThreshold(lowStockThreshold != null ? lowStockThreshold : owner.getLowStockThreshold());

        return itemRepository.save(item);
    }

}