package com.ivm.inventory_management_system.controller;

import com.ivm.inventory_management_system.dto.CustomerCartResponse;
import com.ivm.inventory_management_system.entity.Cart;
import com.ivm.inventory_management_system.service.CartService;
import com.ivm.inventory_management_system.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final NotificationService notificationService;

    public CartController(CartService cartService, NotificationService notificationService) {
        this.cartService = cartService;
        this.notificationService = notificationService;
    }

    public record AddToCartRequest(String customerName, Long itemId, Integer quantity) {}
    public record MessageResponse(String message, Object data) {}

    @PostMapping("/customer/{ownerId}/add")
    public ResponseEntity<?> addToCart(@PathVariable Long ownerId,
                                       @RequestBody AddToCartRequest req) {
        try {
            Cart saved = cartService.addToCart(ownerId, req.customerName(), req.itemId(), req.quantity());
            return ResponseEntity.ok(
                    new MessageResponse("Items reserved for 30 minutes. Confirm before expiry.", saved.getId())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{ownerId}/{customerName}")
    public ResponseEntity<CustomerCartResponse> getCustomerCart(
            @PathVariable Long ownerId,
            @PathVariable String customerName) {
        return ResponseEntity.ok(cartService.getCustomerCartItems(ownerId, customerName));
    }

    @PostMapping("/customer/{ownerId}/{customerName}/send-request")
    public ResponseEntity<?> sendRequestToOwner(@PathVariable Long ownerId,
                                                @PathVariable String customerName) {
        try {
            String message = "Customer " + customerName + " has items in cart. Accept or reject within 30 mins.";
            notificationService.sendNotification(customerName, ownerId, message);
            return ResponseEntity.ok(new MessageResponse("Request sent to owner. Await confirmation.", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/customer/{cartId}")
    public ResponseEntity<Void> removeCartLine(@PathVariable Long cartId) {
        cartService.removeCartLine(cartId);
        return ResponseEntity.noContent().build();
    }
}
