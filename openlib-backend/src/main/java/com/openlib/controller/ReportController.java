package com.openlib.controller;

import com.openlib.domain.User;
import com.openlib.dto.response.BuyerReportResponse;
import com.openlib.dto.response.MetricsResponse;
import com.openlib.dto.response.SellerReportResponse;
import com.openlib.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

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

    /**
     * Obtiene métricas de ventas para un período determinado
     * @param startDate Fecha de inicio (formato: yyyy-MM-dd'T'HH:mm:ss)
     * @param endDate Fecha de fin (formato: yyyy-MM-dd'T'HH:mm:ss)
     */
    @GetMapping("/metrics")
    public MetricsResponse getMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        // Si no se proporcionan fechas, usar últimos 30 días
        LocalDateTime end = endDate != null ? endDate : LocalDateTime.now();
        LocalDateTime start = startDate != null ? startDate : end.minusDays(30);
        
        return reportService.getMetricsInPeriod(start, end);
    }
}
