package com.ivm.inventory_management_system.service;

import com.ivm.inventory_management_system.entity.*;
import com.ivm.inventory_management_system.repository.CartRepository;
import com.ivm.inventory_management_system.repository.ItemRepository;
import com.ivm.inventory_management_system.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ItemRepository itemRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository,
                        ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public Order createOrderFromAcceptedNotification(Long ownerId, String customerName) {
        Optional<Order> existingOrder = orderRepository
                .findTopByOwnerIdAndCustomerNameOrderByCreatedAtDesc(ownerId, customerName);

        boolean hasActiveOrder = existingOrder.stream()
                .anyMatch(o -> o.getCompletedAt() == null && o.getCancelledAt() == null);
        if (hasActiveOrder) {
            throw new RuntimeException("An active order already exists for this customer.");
        }

        List<Cart> cartItems = cartRepository.findByOwnerIdAndCustomerName(ownerId, customerName);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("No cart items to create order");
        }

        Order order = new Order();
        order.setOwner(cartItems.get(0).getOwner());
        order.setCustomerName(customerName);

        var orderItems = cartItems.stream().map(c -> {
            OrderItem oi = new OrderItem();
            oi.setItem(c.getItem());
            oi.setQuantity(c.getQuantity());
            oi.setOrder(order);
            return oi;
        }).toList();

        order.setItems(orderItems);

        Order saved = orderRepository.save(order);
        return saved;
    }

        @Transactional
        public Order completeOrder(Long ownerId, String customerName) {
            Order order = orderRepository.findByOwnerIdAndCustomerName(ownerId, customerName).
                    orElseThrow(()-> new RuntimeException("Order not found."));

            for (OrderItem oi : order.getItems()) {
                Item item = oi.getItem();
                itemRepository.save(item);
            }

            orderRepository.save(order);
            cartRepository.deleteAll(cartRepository.findByOwnerIdAndCustomerName(order.getOwner().getId(), order.getCustomerName()));
            return order;
        }
}
