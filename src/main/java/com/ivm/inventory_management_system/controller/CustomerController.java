package com.ivm.inventory_management_system.controller;

import com.ivm.inventory_management_system.dto.CustomerItemDto;
import com.ivm.inventory_management_system.service.ItemService;
import com.ivm.inventory_management_system.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    private ItemService itemService;
    private UserService userService;

    public CustomerController(ItemService itemService, UserService userService) {
        this.itemService = itemService;
        this.userService = userService;
    }

    @GetMapping("/owner/{ownerId}/items")
    public ResponseEntity<?> getItemsForCustomer(@PathVariable Long ownerId) {
        return userService.findById(ownerId)
                .map(owner -> {
                    List<CustomerItemDto> items = itemService.getItemsForCustomer(owner.getBusinessType());
                    return ResponseEntity.ok(items);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerId}/items/search")
    public ResponseEntity<?> searchItemsForCustomer(
            @PathVariable Long ownerId,
            @RequestParam String keyword) {

        return userService.findById(ownerId)
                .map(owner -> ResponseEntity.ok(
                        itemService.searchItemsForCustomer(keyword, owner.getBusinessType())
                ))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}