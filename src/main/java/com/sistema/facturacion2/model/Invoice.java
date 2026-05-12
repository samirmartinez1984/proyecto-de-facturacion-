package com.sistema.facturacion2.model;

import com.sistema.facturacion2.model.auth.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una factura dentro del sistema de facturación.
 *
 * <p>Concentra la cabecera del documento comercial, incluyendo su identificación
 * funcional, estado, montos calculados, usuario asociado y los ítems que forman
 * el detalle de la operación.</p>
 */
@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    /**
     * Identificador funcional único de la factura dentro del negocio.
     */
    @Column(name = "folio", unique = true, nullable = false)
    private String folio;
    
    @Column(name = "description")
    private String description;
    
    /**
     * Estado actual del ciclo de vida de la factura.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status;
    
    /**
     * Importe base de la factura antes de impuestos.
     */
    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    /**
     * Valor total de impuestos calculados sobre la operación.
     */
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;
    
    /**
     * Importe final de la factura, incluyendo impuestos.
     */
    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;
    
    /**
     * Fecha y hora de creación del documento, asignada por el backend.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Usuario responsable o asociado a la emisión de la factura.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    /**
     * Detalle de líneas que componen la factura.
     */
    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<InvoiceItem> items = new ArrayList<>();


    /**
     * Agrega un ítem a la factura y sincroniza ambos lados de la relación bidireccional.
     *
     * @param item ítem que se incorporará al detalle de la factura
     */
    public void addItem(InvoiceItem item) {
        this.items.add(item);
        item.setInvoice(this); // Sincroniza ambos lados en memoria
    }
    
    /**
     * Asigna automáticamente la fecha de creación antes de persistir la factura.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
