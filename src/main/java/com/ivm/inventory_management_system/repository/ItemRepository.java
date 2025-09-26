package com.ivm.inventory_management_system.repository;

import com.ivm.inventory_management_system.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByUserId(Long id);
    List<Item> findByNameContainingIgnoreCase(String name);
    List<Item> findByCategory_NameContainingIgnoreCase(String categoryName);

    List<Item> findByUserIdAndNameContainingIgnoreCase(Long userId, String keyword);
    List<Item> findByUserIdAndCategoryNameContainingIgnoreCase(Long userId, String keyword);

    Optional<Item> findByUserIdAndName(Long userId, String name);
}
