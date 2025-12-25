package com.bronx.notification.repository;

import com.bronx.notification.model.entity.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository
        extends JpaRepository<SubscriptionPlan, Long> {
    Optional<SubscriptionPlan> findByCode(String code);

    @Query("select s from SubscriptionPlan s where (s.name is null or lower(s.name) like lower(concat('%', ?1, '%')))")
    Page<SubscriptionPlan> findAllByName(String name, Pageable pageable);
}

