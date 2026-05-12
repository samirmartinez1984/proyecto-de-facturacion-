package com.sistema.facturacion2.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Entidad que representa un producto comercializable dentro del sistema.
 *
 * <p>Almacena la información base necesaria para exponer el catálogo, controlar
 * disponibilidad lógica mediante el campo {@code enabled} y mantener datos de
 * auditoría relacionados con su fecha de creación.</p>
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    /**
     * Nombre comercial del producto.
     */
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    /**
     * Precio unitario vigente del producto dentro del catálogo.
     */
    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;
    
    /**
     * Cantidad disponible en inventario.
     */
    @Column(name = "stock")
    private Integer stock;
    
    /**
     * Indica si el producto se encuentra habilitado para uso dentro del sistema.
     */
    @Column(name = "enabled")
    private Boolean enabled;
    
    /**
     * Fecha y hora de creación del registro, asignada por el backend.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Inicializa los valores automáticos antes de persistir el producto.
     *
     * <p>Asigna la marca temporal de creación y, si no se indicó explícitamente,
     * deja el producto habilitado por defecto.</p>
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(ZoneId.of("America/Bogota"));
        if (enabled == null) {
            enabled = true;
        }
    }
}
