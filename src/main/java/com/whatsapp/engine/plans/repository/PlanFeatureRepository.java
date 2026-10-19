
package com.whatsapp.engine.plans.repository;

import com.whatsapp.engine.plans.PlanFeature;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanFeatureRepository extends JpaRepository<PlanFeature, UUID> {

    List<PlanFeature> findByPlanId(UUID planId);
}
