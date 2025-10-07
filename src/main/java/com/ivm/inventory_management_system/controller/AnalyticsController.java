package com.ivm.inventory_management_system.controller;

import com.ivm.inventory_management_system.service.AnalyticsService;
import com.ivm.inventory_management_system.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserService userService;

    public AnalyticsController(AnalyticsService analyticsService, UserService userService) {
        this.analyticsService = analyticsService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @RequestParam(defaultValue = "today") String mode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Principal principal) {
        String email = principal.getName();
        Long ownerId = userService.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

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
