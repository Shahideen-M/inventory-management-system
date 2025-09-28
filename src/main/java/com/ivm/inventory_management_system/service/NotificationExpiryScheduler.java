package com.ivm.inventory_management_system.service;

import com.ivm.inventory_management_system.controller.NotificationWebSocketController;
import com.ivm.inventory_management_system.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class NotificationExpiryScheduler {

    private final NotificationRepository repository;
    private final NotificationWebSocketController wsController;

    public NotificationExpiryScheduler(NotificationRepository repository,
                                       NotificationWebSocketController wsController) {
        this.repository = repository;
        this.wsController = wsController;
    }

    @Scheduled(fixedRate = 60000) // every 1 min
    public void expireNotifications() {
        LocalDateTime now = LocalDateTime.now();
        repository.findAll().stream()
                .filter(n -> !n.isCompleted() && n.getExpiresAt().isBefore(now) && !n.isRejected())
                .forEach(n -> {
                    n.setRejected(true); // auto-expire
                    repository.save(n);
                    wsController.push(n); // live update
                });
    }
}
