package com.sistema.facturacion2.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * DTO de solicitud utilizada para crear un nuevo rol dentro del sistema.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleDTO {
    /**
     * Nombre del rol a registrar.
     */
    @NotBlank(message = "el nombre debe comenzar con ROLES_")
    @Length(min = 3, max = 50)
    private String name;
}
