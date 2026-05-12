package com.sistema.facturacion2.controller;

import com.sistema.facturacion2.dto.response.InvoiceItemDTO;
import com.sistema.facturacion2.service.InvoiceItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la consulta de ítems de factura.
 *
 * <p>Expone los endpoints bajo la ruta base {@code /invoice-items}.
 * Todos los endpoints son de solo lectura y están disponibles tanto para
 * {@code ROLE_ADMIN} como para {@code ROLE_USER}.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/invoice-items")
public class InvoiceItemController {

    private final InvoiceItemService invoiceItemService;

    /**
     * Retorna todos los ítems pertenecientes a una factura específica.
     *
     * @param invoiceId identificador de la factura
     * @return {@code 200 OK} con la lista de {@link InvoiceItemDTO} asociados a la factura;
     *         lista vacía si la factura no tiene ítems
     */
    @GetMapping("/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<InvoiceItemDTO>> findByItemInvoice(@PathVariable Long invoiceId){
        return ResponseEntity.ok(invoiceItemService.findByInvoiceId(invoiceId));
    }

    /**
     * Retorna todos los ítems de factura que contienen un producto con el nombre indicado.
     *
     * <p>El nombre se envía como parámetro de query. Ejemplo:
     * {@code GET /invoice-items/product?name=Laptop}</p>
     *
     * @param name nombre del producto a buscar
     * @return {@code 200 OK} con la lista de {@link InvoiceItemDTO} que coinciden;
     *         lista vacía si no se encuentra ninguna coincidencia
     */
    @GetMapping("/product")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<InvoiceItemDTO>> findByProductName(@RequestParam String name){
        return ResponseEntity.ok(invoiceItemService.findByProductName(name));
    }
}
