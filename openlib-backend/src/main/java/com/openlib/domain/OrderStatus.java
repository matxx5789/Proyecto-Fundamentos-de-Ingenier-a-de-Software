package com.openlib.domain;

/**
 * Estado del flujo de checkout.
 */
public enum OrderStatus {
    PENDING,    // Creada, pendiente de confirmar pago
    CONFIRMED,  // Pago confirmado
    COMPLETED,  // Descarga realizada
    CANCELLED   // Cancelada
}
