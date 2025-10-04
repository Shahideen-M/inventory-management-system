package com.ivm.inventory_management_system.controller;

import com.ivm.inventory_management_system.entity.Notification;
import com.ivm.inventory_management_system.service.NotificationService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping("/owner/{ownerId}")
    public List<Notification> getPending(@PathVariable Long ownerId) {
        return service.getPendingNotifications(ownerId);
    }

    @PostMapping("/{id}/accept")
    public Notification accept(@PathVariable Long id) {
        return service.acceptNotification(id);
    }

    @PostMapping("/{id}/reject")
    public Notification reject(@PathVariable Long id) {
        return service.rejectNotification(id);
    }

    @PostMapping("/{id}/complete")
    public Notification complete(@PathVariable Long id) {
        return service.completeNotification(id);
    }
}
