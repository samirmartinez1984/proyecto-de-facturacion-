package com.sistema.facturacion2.model.auth;

import com.sistema.facturacion2.model.Invoice;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidad que representa a un usuario del sistema.
 *
 * <p>Centraliza la identidad del usuario, sus credenciales, su estado de
 * habilitación, los roles asignados para control de acceso y las facturas
 * relacionadas con su actividad dentro del negocio.</p>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    /**
     * Nombre único de usuario utilizado para autenticación o identificación funcional.
     */
    @Column(name = "username", unique = true, nullable = false)
    private String username;
    
    /**
     * Contraseña persistida del usuario.
     */
    @Column(name = "password", nullable = false)
    private String password;
    
    /**
     * Correo electrónico único asociado al usuario.
     */
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    
    /**
     * Indica si la cuenta del usuario se encuentra habilitada.
     */
    @Column(name = "enabled")
    private Boolean enabled;
    
    /**
     * Roles asignados al usuario para control de acceso dentro del sistema.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    @ToString.Exclude
    private Set<Role> roles = new HashSet<>();

    /**
     * Facturas asociadas al usuario dentro del dominio de facturación.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Invoice> invoices = new ArrayList<>();
    
    /**
     * Fecha y hora de creación del usuario, asignada automáticamente por el backend.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Inicializa la auditoría básica antes de persistir el usuario.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(ZoneId.of("America/Bogota"));
    }
}
