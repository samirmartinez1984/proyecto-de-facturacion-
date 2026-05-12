package com.sistema.facturacion2.controller;

import com.sistema.facturacion2.controller.api.ProductApi;
import com.sistema.facturacion2.dto.request.CreateProductDTO;
import com.sistema.facturacion2.dto.request.UpdateProductDTO;
import com.sistema.facturacion2.dto.response.PageResponseDTO;
import com.sistema.facturacion2.dto.response.ProductDTO;
import com.sistema.facturacion2.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión del catálogo de productos.
 *
 * <p>Implementa {@link ProductApi} que centraliza toda la documentación
 * OpenAPI/Swagger, manteniendo este controller enfocado únicamente
 * en la lógica de delegación al {@link ProductService}.</p>
 *
 * <p>Control de acceso por rol mediante {@code @PreAuthorize}:</p>
 * <ul>
 *   <li>Operaciones de escritura (crear, actualizar, eliminar) → solo {@code ROLE_ADMIN}</li>
 *   <li>Operaciones de consulta (listar, buscar) → {@code ROLE_ADMIN} o {@code ROLE_USER}</li>
 * </ul>
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController implements ProductApi {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody CreateProductDTO createProductDTO) {
        return ResponseEntity.ok(productService.createProduct(createProductDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody UpdateProductDTO updateProductDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, updateProductDTO));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<PageResponseDTO<ProductDTO>> listarProduct(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        return ResponseEntity.ok(productService.findAllProducts(page, size, sortBy));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ProductDTO> listarProductId(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarProduct(@PathVariable Long id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
