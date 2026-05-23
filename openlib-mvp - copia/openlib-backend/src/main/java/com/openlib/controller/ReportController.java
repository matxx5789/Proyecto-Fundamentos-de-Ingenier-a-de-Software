package com.openlib.controller;

import com.openlib.domain.User;
import com.openlib.dto.response.BuyerReportResponse;
import com.openlib.dto.response.SellerReportResponse;
import com.openlib.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/buyer")
    public BuyerReportResponse buyerReport(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return reportService.getBuyerReport(currentUser.getId());
    }

    @GetMapping("/seller")
    public SellerReportResponse sellerReport(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return reportService.getSellerReport(currentUser.getId());
    }
}
