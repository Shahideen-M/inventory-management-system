package com.ivm.inventory_management_system.controller;

import com.ivm.inventory_management_system.dto.ManageItemRequest;
import com.ivm.inventory_management_system.entity.Item;
import com.ivm.inventory_management_system.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.getItemById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/owner/{ownerId}/manage")
    public ResponseEntity<Item> manageItem(
            @PathVariable Long ownerId,
            @RequestBody ManageItemRequest req) {
        try {
            Item saved = itemService.manageItemForOwner(ownerId, req.name(), req.price(), req.quantity(), req.lowStockThreshold());
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("{itemId}")
    public ResponseEntity<String> deleteItem(@PathVariable Long itemId) {
        return itemService.getItemById(itemId)
                .map(item -> {
                    itemService.deleteItem(itemId);
                    return ResponseEntity.ok("Item got deleted.");
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}