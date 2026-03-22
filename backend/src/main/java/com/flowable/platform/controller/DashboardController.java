package com.flowable.platform.controller;

import com.flowable.platform.dto.ApiResponse;
import com.flowable.platform.entity.Dashboard;
import com.flowable.platform.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboards")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listDashboards(Authentication auth) {
        List<Dashboard> dashboards = dashboardService.getUserDashboards(auth.getName());
        List<Dashboard> publicDashboards = dashboardService.getPublicDashboards();

        // Merge and deduplicate
        List<Map<String, Object>> result = dashboards.stream()
                .map(this::toMap)
                .collect(Collectors.toList());

        publicDashboards.stream()
                .filter(pd -> dashboards.stream().noneMatch(d -> d.getId().equals(pd.getId())))
                .map(this::toMap)
                .forEach(result::add);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createDashboard(
            Authentication auth, @RequestBody Map<String, String> body) {
        Dashboard dashboard = dashboardService.createDashboard(
                auth.getName(), body.get("name"), body.get("description"), body.get("layoutJson"));
        return ResponseEntity.ok(ApiResponse.success(toMap(dashboard)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateDashboard(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        Dashboard dashboard = dashboardService.updateDashboard(id, body.get("name"), body.get("layoutJson"));
        return ResponseEntity.ok(ApiResponse.success(toMap(dashboard)));
    }

    private Map<String, Object> toMap(Dashboard d) {
        return Map.of(
                "id", d.getId().toString(),
                "name", d.getName(),
                "description", d.getDescription() != null ? d.getDescription() : "",
                "layoutJson", d.getLayoutJson(),
                "isPublic", d.getIsPublic(),
                "createdAt", d.getCreatedAt().toString()
        );
    }
}
