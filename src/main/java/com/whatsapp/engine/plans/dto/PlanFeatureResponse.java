package com.whatsapp.engine.plans.dto;

import java.util.UUID;

public record PlanFeatureResponse(
        UUID id,
        String featureName,
        String featureValue
) {
}
