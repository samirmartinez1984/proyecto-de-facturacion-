package com.sistema.facturacion2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de respuesta que representa una línea de detalle de factura.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemDTO {
    
    /**
     * Identificador único del ítem facturado.
     */
    private Long id;

    /**
     * Nombre del producto registrado en la factura.
     */
    private String productName;

    /**
     * Cantidad de unidades incluidas en la línea.
     */
    private Integer quantity;

    /**
     * Precio unitario aplicado al ítem.
     */
    private BigDecimal unitPrice;

    /**
     * Total monetario de la línea facturada.
     */
    private BigDecimal lineTotal;
}
