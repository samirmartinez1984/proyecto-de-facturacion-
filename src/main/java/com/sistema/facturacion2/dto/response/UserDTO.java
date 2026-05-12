package com.sistema.facturacion2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO de respuesta que expone la información pública de un usuario.
 *
 * <p>Se utiliza para devolver al cliente los datos principales del usuario,
 * incluyendo sus roles asignados y la información básica de auditoría.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    /**
     * Identificador único del usuario.
     */
    private Long id;

    /**
     * Nombre de usuario visible dentro del sistema.
     */
    private String username;

    /**
     * Correo electrónico asociado al usuario.
     */
    private String email;

    /**
     * Indica si la cuenta del usuario se encuentra habilitada.
     */
    private Boolean enabled;

    /**
     * Conjunto de roles asignados al usuario.
     */
    private Set<RoleDTO> roles;

    /**
     * Fecha y hora de creación del registro.
     */
    private LocalDateTime createdAt;

}
