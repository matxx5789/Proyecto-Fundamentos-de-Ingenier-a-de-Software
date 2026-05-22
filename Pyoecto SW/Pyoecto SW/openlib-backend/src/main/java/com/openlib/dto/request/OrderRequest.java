package com.openlib.dto.request;

import com.openlib.domain.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(

    @NotBlank(message = "El nombre de facturación es obligatorio")
    String billingFullName,

    @NotBlank @Email
    String billingEmail,

    @NotBlank
    String billingAddress,

    @NotBlank
    String billingCity,

    @NotBlank
    String billingCountry,

    @NotNull(message = "El método de pago es obligatorio")
    PaymentMethod paymentMethod,

    String notes
) {}
