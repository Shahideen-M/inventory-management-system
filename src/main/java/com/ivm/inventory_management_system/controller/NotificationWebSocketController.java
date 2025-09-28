package com.ivm.inventory_management_system.controller;

import com.ivm.inventory_management_system.entity.Notification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void push(Notification notification) {
        messagingTemplate.convertAndSend(
                "/topic/owner/" + notification.getOwnerId(),
                notification
        );
    }
}
