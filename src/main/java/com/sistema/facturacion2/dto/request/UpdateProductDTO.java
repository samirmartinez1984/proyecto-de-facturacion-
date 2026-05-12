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
 * DTO de solicitud utilizada para actualizar la información editable de un producto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductDTO {

    /**
     * Nombre actualizado del producto.
     */
    @NotBlank(message = "el nombre del producto no puede estar vacío")
    @Length( min = 3, max = 100)
    private String name;

    /**
     * Descripción actualizada del producto.
     */
    @NotBlank(message = "la descripción del producto no puede estar vacía")
    @Length(max = 500)
    private String description;

    /**
     * Precio unitario actualizado del producto.
     */
    @NotNull(message = "el precio del producto no puede ser nulo")
    @Positive(message = "el precio debe ser siempre positivo")
    private BigDecimal price;

    /**
     * Cantidad disponible en inventario tras la actualización.
     */
    private Integer stock;

    /**
     * Estado de habilitación del producto.
     */
    private Boolean enabled;


}
