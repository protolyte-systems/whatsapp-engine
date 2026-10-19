package com.whatsapp.engine.subscription.controller;

import com.whatsapp.engine.auth.User;
import com.whatsapp.engine.common.api.ApiResponse;
import com.whatsapp.engine.subscription.dto.SubscriptionRequest;
import com.whatsapp.engine.subscription.dto.SubscriptionResponse;
import com.whatsapp.engine.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscriptions")
@PreAuthorize("hasRole('ORG_ADMIN')")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getCurrentSubscription(
            @AuthenticationPrincipal User user
    ) {
        UUID organizationId = user.getOrganization().getId();

        SubscriptionResponse response =
                subscriptionService.getCurrentSubscription(organizationId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Current subscription retrieved successfully",
                        response
                )
        );
    }

    @PostMapping("/plan-change")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> requestPlanChange(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SubscriptionRequest request
    ) {
        UUID organizationId = user.getOrganization().getId();

        SubscriptionResponse response =
                subscriptionService.requestPlanChange(organizationId, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Plan change request submitted successfully",
                        response
                )
        );
    }
}
