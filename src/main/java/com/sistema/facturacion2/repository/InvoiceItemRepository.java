package com.sistema.facturacion2.repository;

import com.sistema.facturacion2.model.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio simple para items de factura
 */
@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    
    /**
     * Obtener todos los items de una factura
     */
    List<InvoiceItem> findByInvoiceId(Long invoiceId);
    
    /**
     * Buscar items por nombre de producto
     */
    List<InvoiceItem> findByProductName(String productName);
}

