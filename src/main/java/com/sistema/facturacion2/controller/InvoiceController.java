package com.sistema.facturacion2.controller;

import com.sistema.facturacion2.dto.request.CreateInvoiceDTO;
import com.sistema.facturacion2.dto.request.UpdateInvoiceDTO;
import com.sistema.facturacion2.dto.response.InvoiceDTO;
import com.sistema.facturacion2.dto.response.InvoiceDetailDTO;
import com.sistema.facturacion2.dto.response.PageResponseDTO;
import com.sistema.facturacion2.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión del módulo de facturación.
 *
 * <p>Expone los endpoints bajo la ruta base {@code /invoices}. Todos los endpoints
 * requieren autenticación y están disponibles tanto para {@code ROLE_ADMIN}
 * como para {@code ROLE_USER}, ya que la facturación es la razón de ser de ambos perfiles.</p>
 */
@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * Crea una nueva factura asociada al usuario autenticado en el contexto de seguridad.
     *
     * <p>La factura se genera en estado {@code DRAFT}, con folio único y totales calculados
     * automáticamente a partir de los ítems enviados.</p>
     *
     * @param createInvoiceDTO datos de la factura a crear (descripción e ítems)
     * @return {@code 201 Created} con el detalle completo de la factura creada como {@link InvoiceDetailDTO}
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<InvoiceDetailDTO> createInvoice(@RequestBody CreateInvoiceDTO createInvoiceDTO){
        return  ResponseEntity.status(201).body(invoiceService.createInvoice(createInvoiceDTO));
    }

    /**
     * Actualiza los datos de una factura existente.
     *
     * <p>Solo se pueden modificar facturas que no estén en estado {@code CANCELLED}.
     * Si se envían nuevos ítems, los totales se recalculan automáticamente.</p>
     *
     * @param id               identificador de la factura a actualizar
     * @param updateInvoiceDTO campos a modificar (descripción, estado, ítems)
     * @return {@code 200 OK} con el detalle actualizado de la factura como {@link InvoiceDetailDTO}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<InvoiceDetailDTO> updateInvoice(@PathVariable Long id, @RequestBody UpdateInvoiceDTO updateInvoiceDTO){
        return ResponseEntity.ok(invoiceService.updateInvoice(id, updateInvoiceDTO));
    }

    /**
     * Retorna una página de todas las facturas registradas en el sistema.
     *
     * <p>Soporta paginación y ordenamiento mediante parámetros opcionales en la
     * query string. Si no se envían, se usan los valores por defecto.</p>
     *
     * @param page   número de página solicitada (base 0, default {@code 0})
     * @param size   cantidad de elementos por página (default {@code 10})
     * @param sortBy campo por el cual ordenar los resultados (default {@code "id"})
     * @return {@code 200 OK} con un {@link PageResponseDTO} que contiene
     *         las facturas de la página y la metadata de paginación
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<PageResponseDTO<InvoiceDTO>> findAllInvoice(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy){
        return ResponseEntity.ok(invoiceService.findAllInvoice(page, size, sortBy));
    }

    /**
     * Busca una factura por su identificador único, incluyendo el detalle de sus ítems.
     *
     * @param id identificador de la factura
     * @return {@code 200 OK} con el {@link InvoiceDetailDTO} encontrado
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<InvoiceDetailDTO> findByIdInvoice(@PathVariable Long id){
       return ResponseEntity.ok(invoiceService.findById(id));
    }

    /**
     * Retorna todas las facturas pertenecientes al usuario autenticado en el contexto de seguridad.
     *
     * <p>El username se extrae automáticamente del token JWT; no requiere parámetros adicionales.</p>
     *
     * @return {@code 200 OK} con la lista de {@link InvoiceDTO} del usuario autenticado;
     *         lista vacía si no tiene facturas registradas
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<InvoiceDTO>> findMyInvoices(){
        return ResponseEntity.ok(invoiceService.findMyInvoices());
    }

    /**
     * Cancela una factura existente cambiando su estado a {@code CANCELLED}.
     *
     * <p>No es posible cancelar una factura que ya esté cancelada o que se encuentre
     * en estado {@code PAID}. La factura no se elimina físicamente del sistema.</p>
     *
     * @param id identificador de la factura a cancelar
     * @return {@code 200 OK} con él {@link InvoiceDetailDTO} en estado cancelado
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<InvoiceDetailDTO> cancelInvoice(@PathVariable Long id){
        return ResponseEntity.ok(invoiceService.cancelInvoice(id));
    }
}
