package com.sistema.facturacion2.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

/**
 * DTO de solicitud que representa un ítem incluido al crear o actualizar una factura.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceItemDTO {
    
    /**
     * Nombre del producto que se registrará en la línea de factura.
     */
    @NotBlank(message = "el nombre del producto no puede estar vacío")
    @Length(min = 3, max = 100)
    private String productName;
    
    /**
     * Cantidad de unidades facturadas.
     */
    @NotNull(message = "la cantidad no puede ser nula")
    @Positive(message = "la cantidad debe ser positiva")
    private Integer quantity;
    
    /**
     * Precio unitario aplicado al ítem.
     */
    @NotNull(message = "el precio unitario no puede ser nulo")
    @Positive(message = "el precio unitario debe ser positivo")
    private BigDecimal unitPrice;
}
