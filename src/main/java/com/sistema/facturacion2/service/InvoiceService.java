package com.sistema.facturacion2.service;

import com.sistema.facturacion2.dto.request.CreateInvoiceDTO;
import com.sistema.facturacion2.dto.request.CreateInvoiceItemDTO;
import com.sistema.facturacion2.dto.request.UpdateInvoiceDTO;
import com.sistema.facturacion2.dto.response.InvoiceDTO;
import com.sistema.facturacion2.dto.response.InvoiceDetailDTO;
import com.sistema.facturacion2.dto.response.PageResponseDTO;
import com.sistema.facturacion2.exception.ConflictoException;
import com.sistema.facturacion2.exception.RecursoNoEncontradoException;
import com.sistema.facturacion2.mapper.InvoiceMapper;
import com.sistema.facturacion2.model.Invoice;
import com.sistema.facturacion2.model.InvoiceItem;
import com.sistema.facturacion2.model.InvoiceStatus;
import com.sistema.facturacion2.model.auth.User;
import com.sistema.facturacion2.repository.InvoiceRepository;
import com.sistema.facturacion2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final InvoiceMapper invoiceMapper;
    private static final BigDecimal TAX_RATE = new BigDecimal("0.19");

    /**
     * Genera un folio único para identificar funcionalmente una factura.
     *
     * <p>El folio tiene el formato {@code FAC-YYYY-XXXXXXXX}, donde {@code YYYY}
     * es el año actual y {@code XXXXXXXX} es una secuencia aleatoria de 8 caracteres
     * en mayúsculas derivada de un UUID.</p>
     *
     * @return cadena con el folio generado
     */
    private String generateFolio(){
        String year = String.valueOf(LocalDate.now().getYear());
        String uniquePart = UUID.randomUUID()
                .toString().replace("-", "")
                .substring(0, 8)
                .toUpperCase();
        return "FAC-" + year + "-" +uniquePart;
    }
    /**
     * Calcula y asigna el subtotal, el impuesto y el total de una factura
     * a partir de sus ítems, sin depender de {@code @PrePersist}.
     *
     * <p>El subtotal se obtiene sumando {@code unitPrice × quantity} de cada ítem.
     * El impuesto se calcula aplicando {@link #TAX_RATE} sobre el subtotal.
     * El total es la suma de ambos.</p>
     *
     * @param invoice factura sobre la que se asignarán los montos calculados
     * @param items   lista de ítems que componen el detalle de la factura
     */
    private void calculateTotals(Invoice invoice, List<InvoiceItem> items){

        // 1. suma todos los line-total de cada item
        BigDecimal subtotal = items.stream()
                .map(item -> item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Calcula el impuesto: subtotal * 19%
        BigDecimal taxAmount = subtotal.multiply(TAX_RATE);

        // Calcula el total: subtotal + taxAmount
        BigDecimal total = subtotal.add(taxAmount);

        // Asigna los 3 valores al objeto
        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotal(total);
    }
    /**
     * Crea una nueva factura en estado {@link InvoiceStatus#DRAFT} asociada
     * al usuario autenticado en el contexto de seguridad.
     *
     * <p>El folio se genera automáticamente, los ítems se construyen a partir
     * del DTO y los totales se calculan antes de persistir.</p>
     *
     * @param dto datos de entrada con la descripción e ítems de la factura
     * @return {@link InvoiceDetailDTO} con la factura creada y sus totales calculados
     * @throws RecursoNoEncontradoException si el usuario autenticado no existe en la BD
     */
    @Transactional
    public InvoiceDetailDTO createInvoice(CreateInvoiceDTO dto){

        // Buscar el usuario por el username
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new RecursoNoEncontradoException(
                        "El usuario no encontrado"));

        // Crea la instancia de una factura (invoice)
        Invoice invoice = new Invoice();
        invoice.setFolio(generateFolio());
        invoice.setUser(user);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setDescription(dto.getDescription());

        for (CreateInvoiceItemDTO itemDTO : dto.getItems()){
            InvoiceItem item = new InvoiceItem();
            item.setProductName(itemDTO.getProductName());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice());
            invoice.addItem(item);
        }
        calculateTotals(invoice, invoice.getItems());
        Invoice invoiceCreada = invoiceRepository.save(invoice);
        return invoiceMapper.toDetailDto(invoiceCreada);
    }
    /**
     * Actualiza una factura existente aplicando los cambios del DTO.
     *
     * <p>Solo se pueden actualizar facturas que no estén en estado
     * {@link InvoiceStatus#CANCELLED}. Si se envían nuevos ítems, los totales
     * se recalculan automáticamente.</p>
     *
     * @param id  identificador de la factura a actualizar
     * @param dto datos con los campos a modificar (descripción, estado, ítems)
     * @return {@link InvoiceDetailDTO} con la factura actualizada
     * @throws RecursoNoEncontradoException si no existe una factura con el ID indicando
     * @throws ConflictoException           si la factura ya está cancelada
     */
    @Transactional
    public InvoiceDetailDTO updateInvoice(Long id, UpdateInvoiceDTO dto){

        Invoice existingInvoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El id no se encuentra con el ID " + id));

        if (existingInvoice.getStatus() == InvoiceStatus.CANCELLED){
            throw new ConflictoException("La factura esta cancelada");
        }
        invoiceMapper.updateEntityFromDto(dto, existingInvoice);

        if (dto.getItems() != null){
            calculateTotals(existingInvoice, existingInvoice.getItems());
        }
        Invoice invoiceUpdate = invoiceRepository.save(existingInvoice);
        return invoiceMapper.toDetailDto(invoiceUpdate);
    }
    /**
     * Retorna una página de facturas registradas en el sistema.
     *
     * <p>Construye un {@link org.springframework.data.domain.PageRequest} con los
     * parámetros recibidos, delega la consulta al repositorio y envuelve el resultado
     * en un {@link PageResponseDTO} con la metadata de paginación.</p>
     *
     * <p><strong>Optimización N+1:</strong> Utiliza {@code @EntityGraph} para cargar
     * el usuario en la misma consulta y evitar consultas adicionales.</p>
     *
     * @param page   número de página solicitada (base 0)
     * @param size   cantidad de elementos por página
     * @param sortBy nombre del campo por el cual ordenar los resultados
     * @return {@link PageResponseDTO} con el contenido de la página y su metadata
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<InvoiceDTO> findAllInvoice(int page, int size, String sortBy){
        var pageable = PageRequest.of(page, size, Sort.by(sortBy));
        var pageResult = invoiceRepository.findAll(pageable);  // ✅ Ahora usa EntityGraph
        List<InvoiceDTO> content = pageResult.getContent()
                .stream()
                .map(invoiceMapper::toDto).toList();
        return new PageResponseDTO<>(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
    }
    /**
     * Busca una factura por su identificador único, cargando sus ítems y usuario en la misma consulta.
     *
     * <p>Utiliza {@code @EntityGraph} para cargar tanto los ítems como el usuario
     * en la misma consulta y evitar completamente el problema N+1.</p>
     *
     * @param id identificador de la factura a buscar
     * @return {@link InvoiceDetailDTO} con la factura y su detalle de ítems
     * @throws RecursoNoEncontradoException si no existe una factura con el ID indicado
     */
    @Transactional(readOnly = true)
    public InvoiceDetailDTO findById(Long id){
        Invoice existingInvoice = invoiceRepository.findById(id)  // ✅ Ahora usa EntityGraph
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La factura no se encuentra con el ID " + id));
        return invoiceMapper.toDetailDto(existingInvoice);
    }
    /**
     * Retorna todas las facturas asociadas al usuario autenticado en el contexto de seguridad.
     *
     * <p>Obtiene él {@code username} desde {@link SecurityContextHolder} y delega
     * la búsqueda al repositorio, devolviendo una vista resumen de cada factura.</p>
     *
     * <p><strong>Optimización N+1:</strong> Utiliza {@code @EntityGraph} para cargar
     * el usuario en la misma consulta y evitar consultas adicionales.</p>
     *
     * @return lista de {@link InvoiceDTO} pertenecientes al usuario autenticado;
     *         lista vacía si no tiene facturas registradas
     */
    @Transactional(readOnly = true)
    public List<InvoiceDTO> findMyInvoices(){
        String username = SecurityContextHolder
                .getContext().getAuthentication()
                .getName();

        return invoiceRepository.findByUsernameWithUser(username)  // ✅ Ahora usa EntityGraph
                .stream().map(invoiceMapper::toDto)
                .toList();
    }
    /**
     * Cancela una factura existente cambiando su estado a {@link InvoiceStatus#CANCELLED}.
     *
     * <p>La factura no se elimina físicamente del sistema; solo se actualiza su estado.
     * No es posible cancelar una factura que ya esté cancelada o que haya sido pagada.</p>
     *
     * @param id identificador de la factura a cancelar
     * @return {@link InvoiceDetailDTO} con la factura en estado cancelado
     * @throws RecursoNoEncontradoException si no existe una factura con el ID indicado
     * @throws ConflictoException           si la factura ya está cancelada o está en estado pagado
     */
    @Transactional
    public InvoiceDetailDTO cancelInvoice(Long id){
        
        Invoice existingInvoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La factura no se encuentra con el ID " + id));
        
        if (existingInvoice.getStatus() == InvoiceStatus.CANCELLED){
            throw new ConflictoException("La factura ya esta cancelada");
        }
        if (existingInvoice.getStatus() == InvoiceStatus.PAID){
            throw new ConflictoException("No se puede cancelar uns factura pagada");
        }
        existingInvoice.setStatus(InvoiceStatus.CANCELLED);
        Invoice invoiceCancelled = invoiceRepository.save(existingInvoice);
        return invoiceMapper.toDetailDto(invoiceCancelled);
    }
}
