package com.whatsapp.engine.subscription.repository;

import com.whatsapp.engine.subscription.Subscription;
import com.whatsapp.engine.subscription.SubscriptionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findFirstByOrganizationIdAndStatusOrderByCreatedAtDesc(
            UUID organizationId,
            SubscriptionStatus status
    );

    List<Subscription> findByOrganizationIdOrderByCreatedAtDesc(
            UUID organizationId
    );
}
