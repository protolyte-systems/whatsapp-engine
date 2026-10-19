
package com.whatsapp.engine.plans.repository;

import com.whatsapp.engine.plans.Plan;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, UUID> {

    List<Plan> findByActiveTrue();

    boolean existsByCodeIgnoreCase(String code);
}
