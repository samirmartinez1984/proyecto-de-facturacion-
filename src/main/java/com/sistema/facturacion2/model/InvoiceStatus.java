package com.sistema.facturacion2.model;

/**
 * Estados posibles dentro del ciclo de vida de una factura.
 */
public enum InvoiceStatus {
    /**
     * La factura fue creada, pero aún no se ha confirmado o pagado.
     */
    DRAFT,

    /**
     * La factura fue procesada exitosamente y se considera pagada.
     */
    PAID,

    /**
     * La factura fue anulada y dejó de tener validez operativa.
     */
    CANCELLED
}
