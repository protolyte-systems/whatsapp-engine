package com.whatsapp.engine.subscription.dto;

import com.whatsapp.engine.subscription.BillingCycle;
import com.whatsapp.engine.subscription.SubscriptionStatus;
import java.time.Instant;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        UUID planId,
        String planName,
        SubscriptionStatus status,
        BillingCycle billingCycle,
        Instant currentPeriodStart,
        Instant currentPeriodEnd,
        Instant startedAt,
        Instant cancelledAt
) {
}
