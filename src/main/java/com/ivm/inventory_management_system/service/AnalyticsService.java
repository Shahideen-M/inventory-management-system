package com.ivm.inventory_management_system.service;

import com.ivm.inventory_management_system.dto.TopItemDto;
import com.ivm.inventory_management_system.entity.Order;
import com.ivm.inventory_management_system.entity.OrderItem;
import com.ivm.inventory_management_system.repository.ItemRepository;
import com.ivm.inventory_management_system.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    public AnalyticsService(OrderRepository orderRepository, ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
    }

    public Map<String, Object> getSmartDashboard(Long ownerId) {
        LocalDate today = LocalDate.now();

        List<Order> todayOrders = orderRepository.findByOwnerIdAndCreatedAtBetween(
                ownerId, today.atStartOfDay(), today.plusDays(1).atStartOfDay());

        boolean isToday = !todayOrders.isEmpty();
        List<Order> orders = isToday ? todayOrders :
                orderRepository.findByOwnerIdAndCreatedAtBetween(
                        ownerId, today.minusDays(1).atStartOfDay(), today.atStartOfDay());

        LocalDate usedDate = isToday ? today : today.minusDays(1);

        return buildSummary(ownerId, orders, usedDate, isToday);
    }

    public Map<String, Object> getWeeklySummary(Long ownerId) {
        LocalDate end = LocalDate.now().plusDays(1);
        LocalDate start = LocalDate.now().minusDays(6);

        List<Order> orders = orderRepository.findByOwnerIdAndCreatedAtBetween(
                ownerId, start.atStartOfDay(), end.atStartOfDay());

        return buildSummary(ownerId, orders, LocalDate.now(), true);
    }

    public Map<String, Object> getRangeSummary(Long ownerId, LocalDate startDate, LocalDate endDate) {
        List<Order> orders = orderRepository.findByOwnerIdAndCreatedAtBetween(
                ownerId, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());

        return buildSummary(ownerId, orders, startDate, true);
    }

    private Map<String, Object> buildSummary(Long ownerId, List<Order> orders,
                                             LocalDate dateUsed, boolean isToday) {
        int orderCount = orders.size();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        Map<String, Long> itemCount = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem orderItem : order.getItems()) {
                BigDecimal line = orderItem.getItem().getPrice()
                        .multiply(BigDecimal.valueOf(orderItem.getQuantity()));
                totalRevenue = totalRevenue.add(line);

                String itemName = orderItem.getItem().getName();
                itemCount.put(itemName, itemCount.getOrDefault(itemName, 0L) + orderItem.getQuantity());
            }
        }

        TopItemDto topItem = itemCount.entrySet().stream()
                .map(e -> new TopItemDto(e.getKey(), e.getValue()))
                .max(Comparator.comparingLong(TopItemDto::getTotalSold))
                .orElse(null);

        Integer threshold = itemRepository.findByUserId(ownerId).stream()
                .findFirst()
                .map(i -> i.getLowStockThreshold() != null ? i.getLowStockThreshold() : 10)
                .orElse(10);

        List<String> lowStock = itemRepository.findByUserId(ownerId)
                .stream()
                .filter(i -> i.getQuantity() != null && i.getQuantity() <= threshold)
                .map(i -> "⚠️ Low stock: " + i.getName() + " (" + i.getQuantity() + " left)")
                .toList();

        Map<String, Object> summary = new HashMap<>();
        summary.put("dateUsed", dateUsed);
        summary.put("isTodayData", isToday);
        summary.put("orderCount", orderCount);
        summary.put("totalRevenue", totalRevenue);
        summary.put("topItem", topItem != null ? topItem : "No sales");
        summary.put("lowStockAlerts", lowStock);

        return summary;
    }
}
