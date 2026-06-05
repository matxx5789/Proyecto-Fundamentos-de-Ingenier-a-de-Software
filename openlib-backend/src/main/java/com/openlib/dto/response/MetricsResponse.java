package com.openlib.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record MetricsResponse(
        // Libros vendidos en el período
        long booksSoldInPeriod,
        
        // Órdenes por estado
        Map<String, Long> ordersByStatus,
        
        // Conversion rate (items vendidos / items en carrito totales)
        double conversionRate,
        
        // Datos adicionales para contexto
        long totalCartItems,
        long totalItemsSold,
        
        // Período consultado
        String periodStart,
        String periodEnd
) {}
