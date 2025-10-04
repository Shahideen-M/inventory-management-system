package com.ivm.inventory_management_system.repository;

import com.ivm.inventory_management_system.entity.Item;
import com.ivm.inventory_management_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);

    Optional<User> findByResetToken(String token);
}