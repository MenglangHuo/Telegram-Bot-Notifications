package com.bronx.notification.repository;

import com.bronx.notification.model.entity.CreditUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditUsageRepository extends JpaRepository<CreditUsage, Long> {

}
