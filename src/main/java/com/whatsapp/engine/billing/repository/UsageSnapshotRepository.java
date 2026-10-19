package com.whatsapp.engine.billing.repository;

import com.whatsapp.engine.billing.UsageSnapshot;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsageSnapshotRepository
        extends JpaRepository<UsageSnapshot, UUID> {

    Optional<UsageSnapshot>
    findFirstByOrganizationIdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqualOrderByPeriodStartDesc(
            UUID organizationId,
            Instant time,
            Instant sameTime
    );

    List<UsageSnapshot> findByOrganizationIdOrderByPeriodStartDesc(
            UUID organizationId
    );
}
