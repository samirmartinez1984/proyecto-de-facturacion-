package com.sistema.facturacion2.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de solicitud utilizado para actualizar datos editables de un usuario.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDTO {
    /**
     * Nuevo correo electrónico del usuario.
     */
    @Email
    @NotBlank(message = "el email de tener @ y no puede estar vacío")
    private String email;

    /**
     * Nuevo estado de habilitación de la cuenta.
     */
    private Boolean enabled;
}
