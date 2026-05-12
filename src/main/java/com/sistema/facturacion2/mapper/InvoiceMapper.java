package com.sistema.facturacion2.mapper;

import com.sistema.facturacion2.dto.request.CreateInvoiceDTO;
import com.sistema.facturacion2.dto.request.CreateInvoiceItemDTO;
import com.sistema.facturacion2.dto.request.UpdateInvoiceDTO;
import com.sistema.facturacion2.dto.response.InvoiceDTO;
import com.sistema.facturacion2.dto.response.InvoiceDetailDTO;
import com.sistema.facturacion2.dto.response.InvoiceItemDTO;
import com.sistema.facturacion2.model.Invoice;
import com.sistema.facturacion2.model.InvoiceItem;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper responsable de convertir entre la entidad {@link Invoice}
 * y sus DTO de entrada/salida.
 */
@Component
@AllArgsConstructor
public class InvoiceMapper {

    private final UserMapper userMapper;

    /**
     * Convertir una entidad {@link Invoice} a {@link InvoiceDTO} (vista resumen).
     *
     * @param entity entidad de factura a transformar
     * @return DTO resumen o {@code null} si la entidad es {@code null}
     */
    public InvoiceDTO toDto(Invoice entity) {
        if (entity == null) {
            return null;
        }
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(entity.getId());
        dto.setFolio(entity.getFolio());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setSubtotal(entity.getSubtotal());
        dto.setTaxAmount(entity.getTaxAmount());
        dto.setTotal(entity.getTotal());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUserId(entity.getUser() != null ? entity.getUser().getId() : null);
        return dto;
    }

    /**
     * Convertir una entidad {@link Invoice} a {@link InvoiceDetailDTO} (vista detalle).
     *
     * @param entity entidad de factura a transformar
     * @return DTO detalle o {@code null} si la entidad es {@code null}
     */
    public InvoiceDetailDTO toDetailDto(Invoice entity) {
        if (entity == null) {
            return null;
        }
        InvoiceDetailDTO detailDTO = new InvoiceDetailDTO();
        detailDTO.setId(entity.getId());
        detailDTO.setFolio(entity.getFolio());
        detailDTO.setDescription(entity.getDescription());
        detailDTO.setStatus(entity.getStatus());
        detailDTO.setSubtotal(entity.getSubtotal());
        detailDTO.setTaxAmount(entity.getTaxAmount());
        detailDTO.setTotal(entity.getTotal());
        detailDTO.setCreatedAt(entity.getCreatedAt());
        detailDTO.setUser(userMapper.toDto(entity.getUser()));
        detailDTO.setItems(toItemDtoList(entity.getItems()));
        return detailDTO;
    }

    /**
     * Convertir un {@link CreateInvoiceDTO} a entidad {@link Invoice}.
     *
     * @param dto DTO de creación de factura
     * @return entidad creada o {@code null} si el DTO es {@code null}
     */
    public Invoice toEntity(CreateInvoiceDTO dto){
        if (dto == null){
            return null;
        }
        Invoice entity = new Invoice();
        entity.setDescription(dto.getDescription());
        if (dto.getItems() != null){
            dto.getItems().stream()
                    .map(this::toItemEntity)
                    .forEach(entity::addItem);
        }
        return entity;
    }

    /**
     * Aplicar cambios de {@link UpdateInvoiceDTO} sobre una entidad existente.
     *
     * @param dto DTO con datos de actualización
     * @param entity entidad destino a modificar
     */
    public void updateEntityFromDto(UpdateInvoiceDTO dto, Invoice entity){
        if (dto == null || entity == null){
            return;
        }
        if (dto.getDescription() != null){
            entity.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null){
            entity.setStatus(dto.getStatus());
        }
        if (dto.getItems() != null){
            entity.getItems().clear();
            dto.getItems().stream()
                    .map(this::toItemEntity)
                    .forEach(entity::addItem);
        }
    }

    /**
     * Convertir un item de creación a entidad {@link InvoiceItem}.
     *
     * @param dto DTO del item
     * @return entidad de item o {@code null} si el DTO es {@code null}
     */
    private InvoiceItem toItemEntity(CreateInvoiceItemDTO dto){
        if (dto == null){
            return null;
        }
        InvoiceItem item = new InvoiceItem();
        item.setProductName(dto.getProductName());
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(dto.getUnitPrice());
        return item;
    }

    /**
     * Convertir una lista de entidades {@link InvoiceItem} a DTOs de item.
     *
     * @param items lista de items de factura
     * @return lista de DTOs; vacía si la lista es nula o vacía
     */
    private List<InvoiceItemDTO> toItemDtoList(List<InvoiceItem> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
    }

    /**
     * Convertir una entidad {@link InvoiceItem} a {@link InvoiceItemDTO}.
     *
     * @param entity entidad de item
     * @return DTO de item o {@code null} si la entidad es {@code null}
     */
    public InvoiceItemDTO toItemDto(InvoiceItem entity){
        if (entity == null){
            return null;
        }
        InvoiceItemDTO itemDTO = new InvoiceItemDTO();
        itemDTO.setId(entity.getId());
        itemDTO.setProductName(entity.getProductName());
        itemDTO.setQuantity(entity.getQuantity());
        itemDTO.setUnitPrice(entity.getUnitPrice());
        itemDTO.setLineTotal(entity.getLineTotal());
        return itemDTO;
    }
}
