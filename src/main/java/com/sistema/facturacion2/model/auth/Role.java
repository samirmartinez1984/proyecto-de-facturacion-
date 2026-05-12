package com.sistema.facturacion2.model.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un rol de seguridad dentro del sistema.
 *
 * <p>Los roles se asignan a los usuarios para definir permisos y capacidades
 * operativas, siguiendo el esquema de control de acceso basado en roles.</p>
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    /**
     * Nombre único del rol, normalmente bajo la convención {@code ROLE_*}.
     */
    @Column(name = "name", unique = true, nullable = false)
    private String name;
    
    /**
     * Fecha y hora de creación del rol, asignada desde el backend.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Inicializa la auditoría básica antes de persistir el rol.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
