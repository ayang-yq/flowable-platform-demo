package com.flowable.platform.controller;

import com.flowable.platform.dto.ApiResponse;
import com.flowable.platform.dto.HomeSummaryDTO;
import com.flowable.platform.service.HomeSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@Tag(name = "Home", description = "Home page dashboard APIs")
@SecurityRequirement(name = "bearerAuth")
public class HomeController {

    private final HomeSummaryService homeSummaryService;

    public HomeController(HomeSummaryService homeSummaryService) {
        this.homeSummaryService = homeSummaryService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get home summary", description = "Returns aggregated dashboard data including pending tasks, active processes, and recent activity")
    public ResponseEntity<ApiResponse<HomeSummaryDTO>> getSummary() {
        HomeSummaryDTO summary = homeSummaryService.getSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
