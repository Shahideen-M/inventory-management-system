package com.ivm.inventory_management_system.service;

import com.ivm.inventory_management_system.controller.NotificationWebSocketController;
import com.ivm.inventory_management_system.entity.Notification;
import com.ivm.inventory_management_system.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class NotificationService {

    private final NotificationRepository repository;
    private final NotificationWebSocketController wsController;

    public NotificationService(NotificationRepository repository,
                               NotificationWebSocketController wsController) {
        this.repository = repository;
        this.wsController = wsController;
    }

    public Notification sendNotification(Long customerId, Long ownerId, String message) {
        Notification n = new Notification();
        n.setCustomerId(customerId);
        n.setOwnerId(ownerId);
        n.setMessage(message);
        n.setExpiresAt(LocalDateTime.now().plusMinutes(30)); // 30-min expiry
        Notification saved = repository.save(n);

        wsController.push(saved); // push live update
        return saved;
    }

    public List<Notification> getPendingNotifications(Long ownerId) {
        LocalDateTime now = LocalDateTime.now();
        return repository.findByOwnerIdAndCompletedFalse(ownerId)
                .stream()
                .filter(n -> n.getExpiresAt().isAfter(now))
                .toList();
    }

    public Notification acceptNotification(Long id) {
        Notification n = repository.findById(id).orElseThrow();
        n.setAccepted(true);
        repository.save(n);
        wsController.push(n); // update UI in real-time
        return n;
    }

    public Notification rejectNotification(Long id) {
        Notification n = repository.findById(id).orElseThrow();
        n.setRejected(true);
        repository.save(n);
        wsController.push(n);
        return n;
    }

    public Notification completeNotification(Long id) {
        Notification n = repository.findById(id).orElseThrow();
        n.setCompleted(true);
        repository.save(n);
        wsController.push(n);
        return n;
    }
}
