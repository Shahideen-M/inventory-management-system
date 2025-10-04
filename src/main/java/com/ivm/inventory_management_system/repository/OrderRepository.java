package com.ivm.inventory_management_system.repository;

import com.ivm.inventory_management_system.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByOwnerId(Long ownerId);
    Optional<Order> findByOwnerIdAndCustomerName(Long ownerId, String customerName);
    Optional<Order> findTopByOwnerIdAndCustomerNameOrderByCreatedAtDesc(Long ownerId, String customerName);

    List<Order> findByOwnerIdAndCreatedAtBetween(Long ownerId, LocalDateTime start, LocalDateTime end);
}