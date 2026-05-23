package com.openlib.dto.response;

import com.openlib.domain.OrderStatus;
import com.openlib.domain.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long            id,
    OrderStatus     status,
    PaymentMethod   paymentMethod,
    BigDecimal      totalAmount,
    String          billingFullName,
    String          billingEmail,
    String          billingAddress,
    String          billingCity,
    String          billingCountry,
    List<OrderItemResponse> items,
    LocalDateTime   createdAt
) {}
