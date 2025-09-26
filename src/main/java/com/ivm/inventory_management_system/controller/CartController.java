package com.ivm.inventory_management_system.controller;

import com.ivm.inventory_management_system.entity.Cart;
import com.ivm.inventory_management_system.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    public record AddToCartRequest(String customerName, Long itemId, Integer quantity) {}

    @PostMapping("owner/{ownerId}/add")
    public ResponseEntity<?> addToCart(
            @PathVariable Long ownerId,
            @RequestBody AddToCartRequest req) {
        try {
            Cart saved = cartService.addToCart(ownerId, req.customerName(), req.itemId(), req.quantity());
            return ResponseEntity.ok(
                    new MessageResponse("Items reserved for 30 minutes. Confirm before expiry.", saved));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    public record MessageResponse(String message, Cart cart) {}

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Cart>> getCartsForOwner(
            @PathVariable Long ownerId,
            @RequestParam(required = false) String customerName) {
        if (customerName == null) {
            return ResponseEntity.ok(cartService.getCartsForOwner(ownerId));
        } else {
            return ResponseEntity.ok(cartService.getCartsForOwnerAndCustomer(ownerId, customerName));
        }
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> removeCartLine(@PathVariable Long cartId) {
        cartService.removeCartLine(cartId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/owner/{ownerId}/clear")
    public ResponseEntity<Void> clearOwnerCart(@PathVariable Long ownerId,
                                               @RequestParam(required = false) String customerName) {
        if (customerName == null) {
            cartService.clearCartsForOwner(ownerId);
        } else {
            cartService.clearCartsForOwnerAndCustomer(ownerId, customerName);
        }
        return ResponseEntity.noContent().build();
    }
}
