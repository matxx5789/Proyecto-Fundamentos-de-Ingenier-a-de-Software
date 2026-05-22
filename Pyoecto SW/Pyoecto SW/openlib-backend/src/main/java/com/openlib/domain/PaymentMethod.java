package com.openlib.domain;

/**
 * Métodos de pago soportados en el checkout simulado.
 */
public enum PaymentMethod {
    FREE,        // Descarga gratuita (precio $0.00)
    DONATION,    // Donación voluntaria
    CREDIT_CARD, // Tarjeta de crédito simulada
    PAYPAL       // PayPal simulado
}
