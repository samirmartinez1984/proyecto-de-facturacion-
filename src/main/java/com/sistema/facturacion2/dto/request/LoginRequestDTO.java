package com.sistema.facturacion2.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de solicitud utilizada para autenticar un usuario en el sistema.
 *
 * <p>Contiene las credenciales mínimas necesarias para el proceso de login:
 * nombre de usuario y contraseña en texto plano (viaja por HTTPS).</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    /**
     * Nombre de usuario registrado en el sistema.
     */
    @NotBlank(message = "El username no puede estar vacío")
    private String username;

    /**
     * Contraseña del usuario en texto plano; será validada contra el hash almacenado.
     */
    @NotBlank(message = "El password no puede estar vacío")
    private String password;
}

