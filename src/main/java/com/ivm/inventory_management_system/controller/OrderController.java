package com.ivm.inventory_management_system.controller;

import com.ivm.inventory_management_system.entity.Order;
import com.ivm.inventory_management_system.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<Order> createOrderFromNotification(@RequestParam Long ownerId,
                                                             @RequestParam String customerName) {
        return ResponseEntity.ok(orderService.createOrderFromAcceptedNotification(ownerId, customerName));
    }

    @PostMapping("/{ownerId}/{customerName}/complete")
    public ResponseEntity<Void> completeOrder(@PathVariable Long ownerId,
                                              @PathVariable String customerName) {
        orderService.completeOrder(ownerId, customerName);
        return ResponseEntity.ok().build();
    }
}