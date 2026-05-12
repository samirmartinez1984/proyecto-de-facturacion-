package com.sistema.facturacion2.mapper;

import com.sistema.facturacion2.dto.request.CreateProductDTO;
import com.sistema.facturacion2.dto.request.UpdateProductDTO;
import com.sistema.facturacion2.dto.response.ProductDTO;
import com.sistema.facturacion2.model.Product;
import org.springframework.stereotype.Component;

/**
 * Mapper responsable de convertir entre la entidad {@link Product}
 * y los DTOs de entrada/salida de producto.
 */
@Component
public class ProductMapper {

    /**
     * Convertir una entidad {@link Product} a {@link ProductDTO}.
     *
     * @param entity entidad de producto a transformar
     * @return DTO de respuesta o {@code null} si la entidad es {@code null}
     */
    public ProductDTO toDto(Product entity){
        if (entity == null){
            return null;
        }
        ProductDTO dto = new ProductDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setEnabled(entity.getEnabled());
        dto.setStock(entity.getStock());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    /**
     * Convertir un {@link CreateProductDTO} a entidad {@link Product}.
     *
     * @param productDTO DTO de creación
     * @return entidad creada o {@code null} si el DTO es {@code null}
     */
    public Product toEntity(CreateProductDTO productDTO){
        if (productDTO == null){
            return null;
        }
        Product entity = new Product();
        entity.setName(productDTO.getName());
        entity.setPrice(productDTO.getPrice());
        entity.setStock(productDTO.getStock());
        entity.setDescription(productDTO.getDescription());
        return entity;
    }

    /**
     * Aplicar cambios de {@link UpdateProductDTO} sobre una entidad existente.
     *
     * @param dto DTO con datos de actualización
     * @param entity entidad destino a modificar
     */
    public void updateEntityFromDto(UpdateProductDTO dto, Product entity){
        if (dto == null || entity == null){
            return;
        }
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setStock(dto.getStock());
        entity.setEnabled(dto.getEnabled());
    }
}
