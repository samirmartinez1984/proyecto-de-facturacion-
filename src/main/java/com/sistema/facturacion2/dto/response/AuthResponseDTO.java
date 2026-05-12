package com.sistema.facturacion2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de respuesta devuelta tras un registro o inicio de sesión exitoso.
 *
 * <p>Encapsula el token JWT generado y la información pública del usuario
 * autenticado para que el cliente pueda almacenarlo y usarlo en peticiones
 * subsecuentes mediante el header {@code Authorization: Bearer <token>}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    /**
     * Token JWT firmado con HS512, válido por el tiempo configurado en
     * {@code app.jwt.expiration}.
     */
    private String token;

    /**
     * Tipo de esquema de autenticación. Siempre {@code "Bearer"}.
     */
    private String type = "Bearer";

    /**
     * Información pública del usuario autenticado (sin contraseña).
     */
    private UserDTO user;

    /**
     * Constructor de conveniencia: asigna token y usuario; el tipo queda en "Bearer".
     *
     * @param token token JWT generado
     * @param user  DTO del usuario autenticado
     */
    public AuthResponseDTO(String token, UserDTO user) {
        this.token = token;
        this.user  = user;
    }
}

