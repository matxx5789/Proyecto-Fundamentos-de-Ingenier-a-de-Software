package com.openlib.domain;

/**
 * Estado de publicación de un libro.
 */
public enum BookStatus {
    PENDING,    // Publicado por seller, esperando aprobación admin
    APPROVED,   // Aprobado y visible en catálogo
    REJECTED,   // Rechazado por admin
    ARCHIVED    // Retirado del catálogo
}
