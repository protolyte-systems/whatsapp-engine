package com.whatsapp.engine.subscription.dto;

import com.whatsapp.engine.subscription.BillingCycle;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SubscriptionRequest(
        @NotNull(message = "Plan ID is required")
        UUID planId,

        @NotNull(message = "Billing cycle is required")
        BillingCycle billingCycle
) {
}
