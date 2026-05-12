package com.sistema.facturacion2.dto.response;

import com.sistema.facturacion2.model.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta detallada para una factura.
 *
 * <p>Extiende la información de la cabecera incorporando el usuario completo y el
 * detalle de ítems asociados al documento.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDetailDTO {
    
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
     * Estado actual del documento.
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
     * Importe final de la factura.
     */
    private BigDecimal total;

    /**
     * Fecha y hora de creación del documento.
     */
    private LocalDateTime createdAt;
    
    /**
     * Información completa del usuario asociado a la factura.
     */
    private UserDTO user;
    
    /**
     * Líneas de detalle que componen la factura.
     */
    private List<InvoiceItemDTO> items;
}
