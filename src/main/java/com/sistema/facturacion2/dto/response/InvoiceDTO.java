package com.sistema.facturacion2.dto.response;

import com.sistema.facturacion2.model.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta resumida para exponer la cabecera de una factura.
 *
 * <p>Se utiliza en listados o respuestas donde no es necesario devolver el detalle
 * completo de ítems, pero sí la información principal del documento y su usuario asociado.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {
    
    /**
     * Identificador único de la factura.
     */
    private Long id;

    /**
     * Folio funcional del documento.
     */
    private String folio;

    /**
     * Descripción general de la factura.
     */
    private String description;

    /**
     * Estado actual de la factura.
     */
    private InvoiceStatus status;

    /**
     * Importe base antes de impuestos.
     */
    private BigDecimal subtotal;

    /**
     * Monto total de impuestos aplicados.
     */
    private BigDecimal taxAmount;

    /**
     * Importe final del documento.
     */
    private BigDecimal total;

    /**
     * Fecha y hora de creación de la factura.
     */
    private LocalDateTime createdAt;
    
    /**
     * Identificador del usuario asociado a la factura.
     */
    private Long userId;
}
