package com.openlib.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record BookRequest(

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 300)
    String title,

    @Size(max = 300)
    String subtitle,

    @NotBlank(message = "El autor es obligatorio")
    @Size(max = 255)
    String author,

    @Size(max = 20, message = "ISBN demasiado largo")
    String isbn,

    String description,

    String coverUrl,

    @Min(value = 1, message = "Páginas inválidas")
    Integer pages,

    @Size(max = 10)
    String language,

    @Min(1900) @Max(2100)
    Integer publishedYear,

    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    BigDecimal price,

    Long categoryId,

    /** IDs de las etiquetas a asignar */
    List<Long> tagIds
) {}
