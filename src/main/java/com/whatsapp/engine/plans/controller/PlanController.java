package com.whatsapp.engine.plans.controller;

import com.whatsapp.engine.common.api.ApiResponse;
import com.whatsapp.engine.plans.dto.PlanResponse;
import com.whatsapp.engine.plans.service.PlanService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getAvailablePlans() {
        List<PlanResponse> plans = planService.getAvailablePlans();

        return ResponseEntity.ok(
                ApiResponse.success("Available plans retrieved successfully", plans)
        );
    }
}
