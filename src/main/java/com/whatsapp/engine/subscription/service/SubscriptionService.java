package com.whatsapp.engine.subscription.service;
import com.whatsapp.engine.organization.Organization;
import com.whatsapp.engine.organization.repository.OrganizationRepository;
import com.whatsapp.engine.plans.Plan;
import com.whatsapp.engine.plans.repository.PlanRepository;
import com.whatsapp.engine.subscription.BillingCycle;
import com.whatsapp.engine.subscription.Subscription;
import com.whatsapp.engine.subscription.SubscriptionStatus;
import com.whatsapp.engine.subscription.dto.SubscriptionRequest;
import com.whatsapp.engine.subscription.dto.SubscriptionResponse;
import com.whatsapp.engine.subscription.repository.SubscriptionRepository;
import com.whatsapp.engine.common.exception.ApplicationException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final OrganizationRepository organizationRepository;

    public SubscriptionService(
            SubscriptionRepository subscriptionRepository,
            PlanRepository planRepository,
            OrganizationRepository organizationRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getCurrentSubscription(UUID organizationId) {
        Subscription subscription = subscriptionRepository
                .findFirstByOrganizationIdAndStatusOrderByCreatedAtDesc(
                        organizationId,
                        SubscriptionStatus.ACTIVE
                )
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.NOT_FOUND,
                        "SUBSCRIPTION_NOT_FOUND",
                        "No active subscription found"
                ));

        return toResponse(subscription);
    }

    @Transactional
    public SubscriptionResponse requestPlanChange(
            UUID organizationId,
            SubscriptionRequest request
    ) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.NOT_FOUND,
                        "ORGANIZATION_NOT_FOUND",
                        "Organization not found"
                ));

        Plan plan = planRepository.findById(request.planId())
                .filter(Plan::isActive)
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.NOT_FOUND,
                        "PLAN_NOT_FOUND",
                        "Active plan not found"
                ));

        Instant now = Instant.now();

        Subscription subscription = new Subscription();
        subscription.setOrganization(organization);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.PENDING);
        subscription.setBillingCycle(request.billingCycle());
        subscription.setCurrentPeriodStart(now);
        subscription.setCurrentPeriodEnd(
                request.billingCycle() == BillingCycle.MONTHLY
                        ? now.plus(1, ChronoUnit.MONTHS)
                        : now.plus(1, ChronoUnit.YEARS)
        );
        subscription.setStartedAt(now);

        Subscription saved = subscriptionRepository.save(subscription);
        return toResponse(saved);
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getPlan().getId(),
                subscription.getPlan().getName(),
                subscription.getStatus(),
                subscription.getBillingCycle(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                subscription.getStartedAt(),
                subscription.getCancelledAt()
        );
    }
}