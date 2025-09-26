package com.ivm.inventory_management_system.repository;

import com.ivm.inventory_management_system.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByOwnerId(Long ownerId);

    List<Order> findByOwnerIdAndCreatedAtBetween(Long ownerId, LocalDateTime start, LocalDateTime end);
}