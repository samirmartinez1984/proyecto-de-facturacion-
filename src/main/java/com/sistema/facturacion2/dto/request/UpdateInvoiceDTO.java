package com.sistema.facturacion2.dto.request;

import com.sistema.facturacion2.model.InvoiceStatus;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO de solicitud utilizada para actualizar una factura existente.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInvoiceDTO {
    
    /**
     * Nueva descripción general de la factura.
     */
    private String description;

    /**
     * Nuevo estado del documento dentro de su ciclo de vida.
     */
    private InvoiceStatus status;
    
    /**
     * Nuevo conjunto de ítems que compondrá la factura.
     */
    @Valid
    private List<CreateInvoiceItemDTO> items;
}
