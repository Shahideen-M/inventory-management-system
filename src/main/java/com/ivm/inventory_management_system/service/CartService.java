package com.ivm.inventory_management_system.service;

import com.ivm.inventory_management_system.entity.Cart;
import com.ivm.inventory_management_system.entity.Item;
import com.ivm.inventory_management_system.entity.User;
import com.ivm.inventory_management_system.repository.CartRepository;
import com.ivm.inventory_management_system.repository.ItemRepository;
import com.ivm.inventory_management_system.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    public CartService(ItemRepository itemRepository,
                       UserRepository userRepository,
                       CartRepository cartRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
    }

    public Cart addToCart(Long ownerId, String customerName, Long itemId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getUser() == null || !item.getUser().getId().equals(ownerId)) {
            throw new RuntimeException("Item does not belong to this owner/shop");
        }
        if (item.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for item: " + item.getName());
        }

        item.setQuantity(item.getQuantity() - quantity);
        itemRepository.save(item);

        Cart cart = new Cart();
        cart.setItem(item);
        cart.setQuantity(quantity);
        cart.setCustomerName(customerName);
        cart.setOwner(owner);
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        return cartRepository.save(cart);
    }

    public List<Cart> getCartsForOwner(Long ownerId) {
        return cartRepository.findByOwnerId(ownerId);
    }

    public List<Cart> getCartsForOwnerAndCustomer(Long ownerId, String customerName) {
        return cartRepository.findByOwnerIdAndCustomerName(ownerId, customerName);
    }

    public void removeCartLine(Long cartId) {
        cartRepository.deleteById(cartId);
    }

    public void clearCartsForOwner(Long ownerId) {
        List<Cart> carts = cartRepository.findByOwnerId(ownerId);
        cartRepository.deleteAll(carts);
    }

    public void clearCartsForOwnerAndCustomer(Long ownerId, String customerName) {
        List<Cart> carts = cartRepository.findByOwnerIdAndCustomerName(ownerId, customerName);
        cartRepository.deleteAll(carts);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void clearExpiredCarts() {
        LocalDateTime now = LocalDateTime.now();
        List<Cart> expiredCarts = cartRepository.findByExpiresAtBefore(now);

        for (Cart cart : expiredCarts) {
            Item item = cart.getItem();
            item.setQuantity(item.getQuantity() + cart.getQuantity());
            itemRepository.save(item);
        }

        cartRepository.deleteAll(expiredCarts);
    }
}