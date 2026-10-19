package com.whatsapp.engine.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BillingSummaryResponse(
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal pendingAmount,
        Instant nextBillingDate,
        String currentBillingCycle
) {
}
