package com.ivm.inventory_management_system.service;

import com.ivm.inventory_management_system.controller.NotificationWebSocketController;
import com.ivm.inventory_management_system.entity.Cart;
import com.ivm.inventory_management_system.entity.Item;
import com.ivm.inventory_management_system.entity.Notification;
import com.ivm.inventory_management_system.repository.CartRepository;
import com.ivm.inventory_management_system.repository.ItemRepository;
import com.ivm.inventory_management_system.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class NotificationService {

    private final NotificationRepository repository;
    private final NotificationWebSocketController wsController;
    private final OrderService orderService;
    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;

    public NotificationService(NotificationRepository repository,
                               NotificationWebSocketController wsController, OrderService orderService, ItemRepository itemRepository, CartRepository cartRepository) {
        this.repository = repository;
        this.wsController = wsController;
        this.orderService = orderService;
        this.itemRepository = itemRepository;
        this.cartRepository = cartRepository;
    }

    public Notification sendNotification(String customerName, Long ownerId, String message) {
        Notification n = new Notification();
        n.setCustomerName(customerName);
        n.setOwnerId(ownerId);
        n.setMessage(message);
        n.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        Notification saved = repository.save(n);

        wsController.push(saved);
        return saved;
    }

    public List<Notification> getPendingNotifications(Long ownerId) {
        LocalDateTime now = LocalDateTime.now();
        return repository.findByOwnerIdAndCompletedFalse(ownerId)
                .stream()
                .filter(n -> !n.isAccepted() && !n.isRejected())
                .filter(n -> n.getExpiresAt().isAfter(now))
                .toList();
    }
    public List<Notification> getPendingNotificationsForOwner(Long ownerId) {
        return repository.findByOwnerIdAndAcceptedFalseAndRejectedFalse(ownerId);
    }


    public Notification acceptNotification(Long id) {
        Notification n = repository.findById(id).orElseThrow();
        n.setAccepted(true);
        n.setMessage("Customer " + n.getCustomerName() + " request accepted.");
        repository.save(n);
        wsController.push(n);
        orderService.createOrderFromAcceptedNotification(n.getOwnerId(), n.getCustomerName());
        return n;
    }

    public Notification rejectNotification(Long id) {
        Notification n = repository.findById(id).orElseThrow();
        n.setRejected(true);
        n.setMessage("Customer " + n.getCustomerName() + " request rejected.");
        n.setCancelledAt(LocalDateTime.now());
        repository.save(n);
        List<Cart> carts = cartRepository.findByOwnerIdAndCustomerName(n.getOwnerId(), n.getCustomerName());
        for (Cart cart : carts) {
            Item item = cart.getItem();
            item.setQuantity(item.getQuantity() + cart.getQuantity());
            itemRepository.save(item);
        }
        cartRepository.deleteAll(carts);
        wsController.push(n);
        return n;
    }

    public Notification completeNotification(Long id) {
        Notification n = repository.findById(id).orElseThrow();
        n.setCompleted(true);
        n.setMessage("Customer " + n.getCustomerName() + " request completed.");
        n.setCompletedAt(LocalDateTime.now());
        repository.save(n);
        wsController.push(n);
        return n;
    }
}