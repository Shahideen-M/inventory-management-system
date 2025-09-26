package com.ivm.inventory_management_system.controller;

import com.ivm.inventory_management_system.entity.Order;
import com.ivm.inventory_management_system.enums.OrderStatus;
import com.ivm.inventory_management_system.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/owner/{ownerId}/confirm")
    public ResponseEntity<?> confirmOrder(
            @PathVariable Long ownerId,
            @RequestParam String customerName) {
        try {
            Order order = orderService.confirmOrder(ownerId, customerName);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/owner/{ownerId}")
    public List<Order> getOrdersForOwner(@PathVariable Long ownerId) {
        return orderService.getOrdersForOwner(ownerId);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId,
                                               @RequestParam OrderStatus status) {
        try {
            Order updated = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
