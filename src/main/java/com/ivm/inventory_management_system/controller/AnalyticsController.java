package com.ivm.inventory_management_system.controller;

import com.ivm.inventory_management_system.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/owner/{ownerId}/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @PathVariable Long ownerId,
            @RequestParam(defaultValue = "today") String mode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Object> response;

        switch (mode.toLowerCase()) {
            case "weekly": response = analyticsService.getWeeklySummary(ownerId);
            break;
            case "range":
                if (startDate == null || endDate == null) {
                    return ResponseEntity.badRequest().body(
                            Map.of("error", "startDate and endDate are required for range mode")
                    );
                }
                response = analyticsService.getRangeSummary(ownerId, startDate, endDate);
                break;
            case "today":
            default: response = analyticsService.getSmartDashboard(ownerId);
            break;
        }
        return ResponseEntity.ok(response);
    }
}
