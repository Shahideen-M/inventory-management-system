package com.ivm.inventory_management_system.service;

import com.ivm.inventory_management_system.dto.CustomerCartResponse;
import com.ivm.inventory_management_system.dto.CustomerCartDto;
import com.ivm.inventory_management_system.entity.Cart;
import com.ivm.inventory_management_system.entity.Item;
import com.ivm.inventory_management_system.entity.Notification;
import com.ivm.inventory_management_system.entity.User;
import com.ivm.inventory_management_system.repository.CartRepository;
import com.ivm.inventory_management_system.repository.ItemRepository;
import com.ivm.inventory_management_system.repository.NotificationRepository;
import com.ivm.inventory_management_system.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final NotificationRepository notificationRepository;

    public CartService(ItemRepository itemRepository,
                       UserRepository userRepository,
                       CartRepository cartRepository, NotificationRepository notificationRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.notificationRepository = notificationRepository;
    }

    public Cart addToCart(Long ownerId, String customerName, Long itemId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!owner.getId().equals(item.getUser().getId())) {
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

    public CustomerCartResponse getCustomerCartItems(Long ownerId, String customerName) {
        List<CustomerCartDto> items = cartRepository.findByOwnerIdAndCustomerName(ownerId, customerName)
                .stream()
                .map(cart -> {
                    CustomerCartDto dto = new CustomerCartDto();
                    dto.setCartId(cart.getId());
                    dto.setCustomerName(cart.getCustomerName());
                    dto.setItemName(cart.getItem().getName());
                    dto.setItemPrice(cart.getItem().getPrice());
                    dto.setQuantity(cart.getQuantity());
                    dto.setStatus(cart.getStatus() != null ? cart.getStatus() : "RESERVED");
                    dto.setExpiresAt(cart.getExpiresAt());
                    dto.setTotalAmount(cart.getItem().getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
                    return dto;
                })
                .toList();

        BigDecimal cartTotal = items.stream()
                .map(CustomerCartDto::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CustomerCartResponse response = new CustomerCartResponse();
        response.setItems(items);
        response.setCartTotal(cartTotal);
        return response;
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void clearExpiredCarts() {
        List<Notification> expiredNotifications =
                notificationRepository.findByExpiresAtBeforeAndCompletedFalse(LocalDateTime.now());

        for (Notification n : expiredNotifications) {
            n.setRejected(true);
            n.setCancelledAt(LocalDateTime.now());
            n.setMessage("Customer " + n.getCustomerName() + " request expired.");
            notificationRepository.save(n);

            List<Cart> expiredCarts = cartRepository.findByExpiresAtBefore(LocalDateTime.now());

            for (Cart cart : expiredCarts) {
                Item item = cart.getItem();
                item.setQuantity(item.getQuantity() + cart.getQuantity());
                itemRepository.save(item);
            }

            cartRepository.deleteAll(expiredCarts);
        }
    }

    public void removeCartLine(Long cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow();
        Item item = cart.getItem();
        item.setQuantity(item.getQuantity() + cart.getQuantity());
        itemRepository.save(item);
        cartRepository.delete(cart);
    }
}
