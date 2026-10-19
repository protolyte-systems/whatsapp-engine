package com.whatsapp.engine.plans.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PlanResponse(
        UUID id,
        String name,
        String code,
        String description,
        BigDecimal price,
        String currency,
        String billingCycle,
        boolean active,
        List<PlanFeatureResponse> features
) {
}
