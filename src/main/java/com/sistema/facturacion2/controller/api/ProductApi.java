package com.sistema.facturacion2.controller.api;

import com.sistema.facturacion2.dto.request.CreateProductDTO;
import com.sistema.facturacion2.dto.request.UpdateProductDTO;
import com.sistema.facturacion2.dto.response.PageResponseDTO;
import com.sistema.facturacion2.dto.response.ProductDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Contrato OpenAPI para los endpoints del catálogo de productos.
 *
 * <p>Esta interfaz centraliza toda la documentación Swagger/OpenAPI
 * de {@code ProductController}, manteniendo el controller limpio
 * y enfocado únicamente en la lógica de delegación al service.</p>
 *
 * <p>Él {@code ProductController} implementa esta interfaz y hereda
 * automáticamente todas las anotaciones de documentación.</p>
 */
@Tag(name = "Productos", description = "Operaciones del catálogo de productos")
public interface ProductApi {

    /**
     * Crea un nuevo producto en el catálogo.
     */
    @Operation(
            summary = "Crear un producto",
            description = "Crea un producto validando que el nombre sea único. Si el nombre ya existe, devuelve un error de conflicto."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "El nombre del producto ya está en uso")
    })
    ResponseEntity<ProductDTO> createProduct(@RequestBody CreateProductDTO createProductDTO);

    /**
     * Actualiza los datos de un producto existente.
     */
    @Operation(
            summary = "Actualizar un producto",
            description = "Modifica los campos del producto indicado. Valida que el nuevo nombre no esté en uso."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "409", description = "El nuevo nombre ya está en uso")
    })
    ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody UpdateProductDTO updateProductDTO);

    /**
     * Retorna una página de productos del catálogo.
     */
    @Operation(
            summary = "Listar productos paginados",
            description = "Retorna una página de productos. Soporta paginación y ordenamiento por campo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos retornada exitosamente")
    })
    ResponseEntity<PageResponseDTO<ProductDTO>> listarProduct(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy);

    /**
     * Busca un producto por su identificador único.
     */
    @Operation(
            summary = "Buscar producto por ID",
            description = "Retorna el detalle de un producto específico según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    ResponseEntity<ProductDTO> listarProductId(@PathVariable Long id);

    /**
     * Elimina un producto del catálogo por su identificador.
     */
    @Operation(
            summary = "Eliminar un producto",
            description = "Elimina el producto indicado por su ID. Si no existe, retorna 404."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    ResponseEntity<Void> eliminarProduct(@PathVariable Long id);
}

