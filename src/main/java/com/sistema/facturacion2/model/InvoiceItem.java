package com.sistema.facturacion2.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad que representa una línea de detalle dentro de una factura.
 *
 * <p>Conserva la información comercial del producto facturado en el momento de
 * la transacción, junto con la cantidad, precio unitario y total de línea.</p>
 */
@Entity
@Table(name = "invoice_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class InvoiceItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    /**
     * Nombre del producto registrado en la factura como dato histórico.
     */
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    /**
     * Cantidad de unidades facturadas para este ítem.
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    /**
     * Precio unitario aplicado al momento de generar la factura.
     */
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    /**
     * Total monetario de la línea, calculado a partir de cantidad por precio unitario.
     */
    @Column(name = "line_total", precision = 10, scale = 2)
    private BigDecimal lineTotal;
    
    /**
     * Factura a la que pertenece este ítem dentro del detalle.
     */
    @ManyToOne
    @JoinColumn(name = "invoice_id")
    @ToString.Exclude
    private Invoice invoice;
    
    /**
     * Recalcula el total de la línea antes de insertar o actualizar el registro.
     *
     * <p>Este callback garantiza consistencia entre la cantidad, el precio unitario
     * y el importe total persistido.</p>
     */
    @PrePersist
    @PreUpdate
    protected void calculateLineTotal() {
        if (quantity != null && unitPrice != null) {
            lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
