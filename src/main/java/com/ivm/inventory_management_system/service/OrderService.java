package com.ivm.inventory_management_system.service;

import com.ivm.inventory_management_system.entity.*;
import com.ivm.inventory_management_system.enums.OrderStatus;
import com.ivm.inventory_management_system.repository.CartRepository;
import com.ivm.inventory_management_system.repository.ItemRepository;
import com.ivm.inventory_management_system.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    public Order confirmOrder(Long ownerId, String customerName) {
        List<Cart> cartItems = cartRepository.findByOwnerIdAndCustomerName(ownerId, customerName);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty for owner " + ownerId + " and customer " + customerName);
        }

        Order order = new Order();
        order.setCustomerName(customerName);
        order.setOwner(cartItems.get(0).getOwner());
        order.setStatus(OrderStatus.PENDING);

        var orderItems = cartItems.stream().map(c -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(c.getItem());
            orderItem.setQuantity(c.getQuantity());
            orderItem.setOrder(order);
            return orderItem;
        }).toList();

        order.setItems(orderItems);
        Order saved = orderRepository.save(order);

        cartRepository.deleteAll(cartItems);

        return saved;
    }

    public List<Order> getOrdersForOwner(Long ownerId) {
        return orderRepository.findByOwnerId(ownerId);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);

        if (status == OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
        } else if (status == OrderStatus.CANCELLED) {
            order.setCancelledAt(LocalDateTime.now());

            for (OrderItem oi : order.getItems()) {
                Item item = itemRepository.findByNameContainingIgnoreCase(oi.getItem().getName()).getFirst();
                item.setQuantity(item.getQuantity() + oi.getQuantity());
                itemRepository.save(item);
            }
        }
        return orderRepository.save(order);
    }
}
