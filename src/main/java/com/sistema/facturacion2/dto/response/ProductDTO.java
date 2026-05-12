package com.sistema.facturacion2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa la información pública de un producto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    /**
     * Identificador único del producto.
     */
    private Long id;

    /**
     * Nombre comercial del producto.
     */
    private String name;

    /**
     * Descripción funcional o comercial del producto.
     */
    private String description;

    /**
     * Precio unitario actual del producto.
     */
    private BigDecimal price;

    /**
     * Cantidad disponible en inventario.
     */
    private Integer stock;

    /**
     * Indica si el producto está habilitado para operar en el sistema.
     */
    private Boolean enabled;

    /**
     * Fecha y hora de creación del producto.
     */
    private LocalDateTime createdAt;
}
