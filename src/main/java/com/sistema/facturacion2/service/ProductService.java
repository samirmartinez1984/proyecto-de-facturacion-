package com.sistema.facturacion2.service;

import com.sistema.facturacion2.dto.request.CreateProductDTO;
import com.sistema.facturacion2.dto.request.UpdateProductDTO;
import com.sistema.facturacion2.dto.response.PageResponseDTO;
import com.sistema.facturacion2.dto.response.ProductDTO;
import com.sistema.facturacion2.exception.ConflictoException;
import com.sistema.facturacion2.exception.RecursoNoEncontradoException;
import com.sistema.facturacion2.mapper.ProductMapper;
import com.sistema.facturacion2.model.Product;
import com.sistema.facturacion2.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de negocio para la gestión del catálogo de productos.
 *
 * <p>Centraliza las reglas de negocio relacionadas con productos:
 * validación de duplicados por nombre, mapeo entre entidades y DTOs,
 * y delegación de la persistencia al {@link ProductRepository}.</p>
 *
 * <h2>Logging estructurado</h2>
 * <p>Utiliza {@code @Slf4j} de Lombok para registrar trazas en tres niveles:</p>
 * <ul>
 *   <li><strong>INFO</strong> — inicio y confirmación exitosa de operaciones
 *       que modifican la base de datos ({@code create}, {@code update}, {@code delete})</li>
 *   <li><strong>WARN</strong> — conflictos de negocio esperados: nombre duplicado
 *       en creación o actualización</li>
 *   <li><strong>ERROR</strong> — situaciones graves como intentos de actualizar
 *       o eliminar un recurso que no existe en la base de datos</li>
 * </ul>
 *
 * <h2>Transaccionalidad</h2>
 * <ul>
 *   <li>Métodos de escritura anotados con {@code @Transactional}</li>
 *   <li>Métodos de lectura anotados con {@code @Transactional(readOnly = true)}</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * Crea y persiste un nuevo producto en el sistema.
     *
     * <p>Válida que no exista otro producto con el mismo nombre antes de guardar.</p>
     *
     * <p><strong>Logging:</strong></p>
     * <ul>
     *   <li>{@code INFO} — al recibir la solicitud de creación</li>
     *   <li>{@code WARN} — si el nombre ya existe (conflicto de negocio)</li>
     *   <li>{@code INFO} — confirmación con el ID asignado al producto creado</li>
     * </ul>
     *
     * @param dto datos del producto a registrar
     * @return {@link ProductDTO} con los datos del producto creado
     * @throws ConflictoException si ya existe un producto con ese nombre
     */
    @Transactional
    public ProductDTO createProduct(CreateProductDTO dto){
        // log de INFO: para rastrear un producto que se intenta crear
        log.info("solicitud para intentar crear nuevo producto: {}", dto.getName());

        if (productRepository.existsByName(dto.getName())){
            // log WARN: Indica una advertencia de negocio, no es un error del sistema, pero el proceso se detiene
            log.warn("No se pudo crear el producto. El '{}'nombre ya existe", dto.getName());
            throw new ConflictoException("El producto " + dto.getName() + " ya existe");
        }


        Product product = productMapper.toEntity(dto);

        Product productCreate = productRepository.save(product);
        // log INFO: Confirmación de éxito
        log.info("producto creado exitosamente con ID: {}", productCreate.getId());
        return productMapper.toDto(productCreate);
    }
    /**
     * Actualiza los campos de un producto existente.
     *
     * <p>Solo modifica los campos que lleguen con valor en el DTO.
     * Válida que el nuevo nombre no esté en uso por otro producto.</p>
     *
     * <p><strong>Logging:</strong></p>
     * <ul>
     *   <li>{@code INFO} — al iniciar la operación con el ID del producto</li>
     *   <li>{@code ERROR} — si el producto no existe en la base de datos</li>
     *   <li>{@code WARN} — si el nuevo nombre ya está en uso por otro producto</li>
     *   <li>{@code INFO} — confirmación de actualización exitosa</li>
     * </ul>
     *
     * @param id  identificador del producto a actualizar
     * @param dto campos a modificar
     * @return {@link ProductDTO} con los datos actualizados
     * @throws RecursoNoEncontradoException si no existe un producto con ese ID
     * @throws ConflictoException si el nuevo nombre ya está en uso
     */
    @Transactional
    public ProductDTO updateProduct(Long id, UpdateProductDTO dto){
        log.info("Iniciando actualización del producto con ID: {}", id);

        // validamos que el producto ya exista en bd
        Product productExistente = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Error al actualizar no se encontro el producto con ID: {}", id);
                    return new RecursoNoEncontradoException(
                            "El producto con el id " + id + " no existe");
                });

        if (dto.getName() != null && !productExistente.getName().equals(dto.getName())
                && productRepository.existsByName(dto.getName())){
            log.warn("Conflicto de actualización el nuevo nombre '{}' ya esta en uso", dto.getName());
            throw new ConflictoException("El producto " + dto.getName() + " ya existe ");
        }
       productMapper.updateEntityFromDto(dto, productExistente);

        Product productoActualizado = productRepository.save(productExistente);
        log.info("Producto con ID: {} actualizado correctamente", id);
        return productMapper.toDto(productoActualizado);
    }
    /**
     * Retorna una página de productos registrados en el catálogo.
     *
     * <p>Construye un {@link org.springframework.data.domain.PageRequest} con los
     * parámetros recibidos, delega la consulta al repositorio y envuelve
     * el resultado en un {@link PageResponseDTO} con la metadata de paginación.</p>
     *
     * @param page    número de página solicitada (base 0)
     * @param size    cantidad de elementos por página
     * @param sortBy  nombre del campo por el cual ordenar los resultados
     * @return {@link PageResponseDTO} con el contenido de la página y su metadata
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<ProductDTO> findAllProducts(int page, int size, String sortBy){
        var pageable = PageRequest.of(page, size, Sort.by(sortBy));
        var pageResult = productRepository.findAll(pageable);
        List<ProductDTO> content = pageResult.getContent()
                .stream()
                .map(productMapper::toDto).toList();
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
     * Busca un producto por su identificador único.
     *
     * @param id identificador del producto
     * @return {@link ProductDTO} con los datos del producto encontrado
     * @throws RecursoNoEncontradoException si no existe un producto con ese ID
     */
    @Transactional(readOnly = true)
    public ProductDTO findById(Long id){
        Product searchedProduct = productRepository
                .findById(id).orElseThrow(() -> new
                        RecursoNoEncontradoException("El producto no existe con el ID " + id));
        return productMapper.toDto(searchedProduct);
    }
    /**
     * Elimina un producto del sistema por su identificador.
     *
     * <p><strong>Logging:</strong></p>
     * <ul>
     *   <li>{@code INFO} — al iniciar el intento de eliminación</li>
     *   <li>{@code ERROR} — si el producto no existe en la base de datos</li>
     * </ul>
     *
     * @param id identificador del producto a eliminar
     * @throws RecursoNoEncontradoException si no existe un producto con ese ID
     */
    @Transactional
    public void deleteById(Long id) {
        log.info("Intentando eliminar un producto con ID: {}", id);
        if (!productRepository.existsById(id)){
            // log de ERROR: Error grave porque se intentó borra algo que no existe en BD
            log.error("Fallo al eliminar: el producto con ID {} no existe en BD", id);
            throw new RecursoNoEncontradoException("El ID" + id + " no existe ");
        }

        productRepository.deleteById(id);
    }
}
