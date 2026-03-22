package com.flowable.platform.controller;

import com.flowable.platform.dto.ApiResponse;
import com.flowable.platform.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/efficiency")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEfficiency() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getTaskCompletionEfficiency()));
    }

    @GetMapping("/bottlenecks")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBottlenecks() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getBottleneckAnalysis()));
    }

    @GetMapping("/distribution")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDistribution() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getProcessDistribution()));
    }

    @GetMapping("/sla-compliance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSlaCompliance() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getSlaComplianceRate()));
    }
}
