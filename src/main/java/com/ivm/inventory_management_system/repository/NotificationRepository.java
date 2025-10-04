package com.ivm.inventory_management_system.repository;

import com.ivm.inventory_management_system.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByOwnerIdAndCompletedFalse(Long ownerId);

    List<Notification> findByOwnerIdAndAcceptedFalseAndRejectedFalse(Long ownerId);

    List<Notification> findByCustomerName(String customerName);

    List<Notification> findByExpiresAtBeforeAndCompletedFalse(LocalDateTime now);
}
