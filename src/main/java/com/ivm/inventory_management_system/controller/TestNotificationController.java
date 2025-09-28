package com.ivm.inventory_management_system.controller;

import com.ivm.inventory_management_system.entity.Notification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestNotificationController {

    private final NotificationWebSocketController webSocketController;

    public TestNotificationController(NotificationWebSocketController webSocketController) {
        this.webSocketController = webSocketController;
    }

    @GetMapping("/send-test-notification")
    public String sendTest() {
        Notification notification = new Notification();
        notification.setOwnerId(1L); // test owner
        notification.setMessage("Hello! This is a test notification.");
        notification.setAccepted(false);
        notification.setCompleted(false);
        notification.setRejected(false);

        webSocketController.push(notification);

        return "Notification sent!";
    }
}
