package com.whatsapp.engine.billing.dto;

import java.time.Instant;
import java.util.UUID;

public record UsageSnapshotResponse(
        UUID id,
        Instant periodStart,
        Instant periodEnd,
        long messagesUsed,
        long contactsUsed,
        long storageUsed
) {
}
