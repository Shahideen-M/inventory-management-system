package com.ivm.inventory_management_system.controller;

import com.ivm.inventory_management_system.dto.OwnerItemDto;
import com.ivm.inventory_management_system.entity.Item;
import com.ivm.inventory_management_system.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/owner/{ownerId}/{itemId}")
    public ResponseEntity<Item> getItemByUserIdAndItemId(@PathVariable Long ownerId,
                                                         @PathVariable Long itemId) {
        return itemService.getItemByUserIdAndId(ownerId, itemId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/owner/{ownerId}/items/manage")
    public ResponseEntity<OwnerItemDto> manageItemForOwner(
            @PathVariable Long ownerId,
            @ModelAttribute OwnerItemDto request) throws IOException {
        try {
            OwnerItemDto dto = itemService.manageItemForOwner(ownerId, request);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException | IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/owner/{ownerId}/items/search")
    public ResponseEntity<List<OwnerItemDto>> searchItemsForOwner(
            @PathVariable Long ownerId,
            @RequestParam String keyword) {

        List<Item> items = itemService.searchItemsForOwner(ownerId, keyword);

        List<OwnerItemDto> dtoList = items.stream().map(item -> {
            OwnerItemDto dto = new OwnerItemDto();
            dto.setName(item.getName());
            dto.setPrice(item.getPrice());
            dto.setQuantity(item.getQuantity());
            dto.setCategory(item.getCategory() != null ? item.getCategory().getName() : null);
            dto.setPrimaryAddress(item.getPrimaryAddress());
            dto.setSecondaryAddress(item.getSecondaryAddress());
            dto.setTertiaryAddress(item.getTertiaryAddress());
            dto.setLowStockThreshold(item.getLowStockThreshold());
            dto.setCreatedAt(item.getCreatedAt());
            dto.setUpdatedAt(item.getUpdatedAt());
            dto.setAvailable(item.getQuantity() > 0);
            return dto;
        }).toList();
        return ResponseEntity.ok(dtoList);
    }

    @DeleteMapping("/{ownerId}/delete/{itemId}")
    public ResponseEntity<String> deleteItem(@PathVariable Long ownerId,
                                             @PathVariable Long itemId) {
        return itemService.getItemByUserIdAndId(ownerId, itemId)
                .map(item -> {
                    itemService.deleteItem(itemId);
                    return ResponseEntity.ok(item.getName()+" got deleted from your inventory.");
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}