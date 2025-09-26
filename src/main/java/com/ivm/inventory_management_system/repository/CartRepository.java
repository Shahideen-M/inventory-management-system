package com.ivm.inventory_management_system.repository;

import com.ivm.inventory_management_system.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByOwnerId(Long ownerId);
    List<Cart> findByOwnerIdAndCustomerName(Long ownerId, String customerName);
    List<Cart> findByExpiresAtBefore(LocalDateTime time);
}
