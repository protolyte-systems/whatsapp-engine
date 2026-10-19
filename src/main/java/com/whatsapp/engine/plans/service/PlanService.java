package com.whatsapp.engine.plans.service;

import com.whatsapp.engine.plans.Plan;
import com.whatsapp.engine.plans.PlanFeature;
import com.whatsapp.engine.plans.dto.PlanFeatureResponse;
import com.whatsapp.engine.plans.dto.PlanResponse;
import com.whatsapp.engine.plans.repository.PlanFeatureRepository;
import com.whatsapp.engine.plans.repository.PlanRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanService {

    private final PlanRepository planRepository;
    private final PlanFeatureRepository planFeatureRepository;

    public PlanService(
            PlanRepository planRepository,
            PlanFeatureRepository planFeatureRepository
    ) {
        this.planRepository = planRepository;
        this.planFeatureRepository = planFeatureRepository;
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> getAvailablePlans() {
        return planRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PlanResponse toResponse(Plan plan) {
        List<PlanFeatureResponse> features =
                planFeatureRepository.findByPlanId(plan.getId())
                        .stream()
                        .map(this::toFeatureResponse)
                        .toList();

        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getCode(),
                plan.getDescription(),
                plan.getPrice(),
                plan.getCurrency(),
                plan.getBillingCycle(),
                plan.isActive(),
                features
        );
    }

    private PlanFeatureResponse toFeatureResponse(PlanFeature feature) {
        return new PlanFeatureResponse(
                feature.getId(),
                feature.getFeatureName(),
                feature.getFeatureValue()
        );
    }
}
