package com.sistema.facturacion2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa un rol expuesto por la API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    /**
     * Identificador único del rol.
     */
    private Long id;

    /**
     * Nombre funcional del rol.
     */
    private String name;

    /**
     * Fecha y hora de creación del rol.
     */
    private LocalDateTime createdAt;
}
