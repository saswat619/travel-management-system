package com.travel.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "partner-inventory-service")
public interface PartnerAnalyticsClient {

    @GetMapping("/api/partner/partners")
    Object getAllPartners(@RequestParam int page, @RequestParam int size);

    @GetMapping("/api/partner/packages")
    Object getAllPackages(@RequestParam int page, @RequestParam int size);
}
