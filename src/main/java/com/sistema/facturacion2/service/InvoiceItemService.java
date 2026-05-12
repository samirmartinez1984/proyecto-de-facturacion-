package com.sistema.facturacion2.service;

import com.sistema.facturacion2.dto.response.InvoiceItemDTO;
import com.sistema.facturacion2.mapper.InvoiceMapper;
import com.sistema.facturacion2.repository.InvoiceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceItemService {

    private final InvoiceItemRepository invoiceItemRepository;
    private final InvoiceMapper invoiceMapper;

    /**
     * Retorna todos los ítems pertenecientes a una factura específica.
     *
     * @param invoiceId identificador de la factura
     * @return lista de {@link InvoiceItemDTO} asociados a la factura;
     *         lista vacía si la factura no tiene ítems
     */
    @Transactional(readOnly = true)
    public List<InvoiceItemDTO> findByInvoiceId(Long invoiceId) {
        return invoiceItemRepository.findByInvoiceId(invoiceId)
                .stream()
                .map(invoiceMapper::toItemDto)
                .toList();
    }
    /**
     * Retorna todos los ítems de factura que contienen un producto con el nombre indicado.
     *
     * <p>Útil para consultar en qué facturas aparece un producto específico.</p>
     *
     * @param name nombre del producto a buscar
     * @return lista de {@link InvoiceItemDTO} cuyos productos coinciden con el nombre;
     *         lista vacía si no se encuentra ninguna coincidencia
     */
    @Transactional(readOnly = true)
    public List<InvoiceItemDTO> findByProductName(String name){
        return invoiceItemRepository.findByProductName(name)
                .stream()
                .map(invoiceMapper::toItemDto)
                .toList();
    }
}
