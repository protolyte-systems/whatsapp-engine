package com.whatsapp.engine.billing.dto;

import com.whatsapp.engine.billing.InvoiceStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        String invoiceNumber,
        InvoiceStatus status,
        BigDecimal amount,
        String currency,
        Instant invoiceDate,
        Instant dueDate,
        Instant paidAt,
        UUID subscriptionId
) {
}
