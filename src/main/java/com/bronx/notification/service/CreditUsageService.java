package com.bronx.notification.service;

import com.bronx.notification.dto.creditUsage.CreditUsageRequest;
import com.bronx.notification.dto.creditUsage.CreditUsageResponse;

import java.util.List;

public interface CreditUsageService {
    CreditUsageResponse create(CreditUsageRequest request);
    List<CreditUsageResponse> findAll();
}
