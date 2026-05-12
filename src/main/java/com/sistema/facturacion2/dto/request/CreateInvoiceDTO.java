package com.sistema.facturacion2.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO de solicitud utilizada para crear una nueva factura.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceDTO {
    
    /**
     * Descripción general o comentario asociado a la factura.
     */
    private String description;
    
    /**
     * Ítems que conforman el detalle inicial de la factura.
     */
    @NotEmpty(message = "la factura debe tener al menos un item")
    @Valid
    private List<CreateInvoiceItemDTO> items;
}
