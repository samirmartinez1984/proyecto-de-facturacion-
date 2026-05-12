package com.sistema.facturacion2.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * DTO de solicitud utilizada para registrar un nuevo usuario en el sistema.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {
    /**
     * Nombre de usuario requerido para la creación.
     */
    @NotBlank(message = "el nombre del usuario no puede estar vacío")
    @Length(min = 3, max = 50)
    private String username;

    /**
     * Contraseña inicial del usuario.
     */
    @NotBlank(message = "el password no puede estar vacío")
    @Length(min = 6, max = 100)
    private String password;

    /**
     * Correo electrónico del usuario a registrar.
     */
    @Email @NotBlank(message = "el email de tener @ y no puede estar vacío")
    private String email;
}
