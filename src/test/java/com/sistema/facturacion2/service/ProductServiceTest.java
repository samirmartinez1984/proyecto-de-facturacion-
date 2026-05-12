package com.sistema.facturacion2.service;

import com.sistema.facturacion2.dto.request.CreateProductDTO;
import com.sistema.facturacion2.dto.request.UpdateProductDTO;
import com.sistema.facturacion2.dto.response.ProductDTO;
import com.sistema.facturacion2.exception.ConflictoException;
import com.sistema.facturacion2.exception.RecursoNoEncontradoException;
import com.sistema.facturacion2.mapper.ProductMapper;
import com.sistema.facturacion2.model.Product;
import com.sistema.facturacion2.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sistema.facturacion2.dto.response.PageResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para {@link ProductService}.
 * <p>
 * Verifica la lógica de negocio de cada método del servicio de productos
 * de forma aislada, usando mocks de {@link com.sistema.facturacion2.repository.ProductRepository}
 * y {@link com.sistema.facturacion2.mapper.ProductMapper} para evitar
 * dependencias con la base de datos.
 * </p>
 * Cubre los siguientes casos:
 * <ul>
 *   <li>Creación de producto — caso feliz y duplicado</li>
 *   <li>Búsqueda por ID — caso feliz y no encontrado</li>
 *   <li>Actualización — caso feliz y no encontrado</li>
 *   <li>Eliminación — caso feliz y no encontrado</li>
 *   <li>Listado paginado — caso feliz</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    /**
     * Verifica que {@code createProduct} retorna un {@link ProductDTO} correctamente
     * cuando el producto no existe previamente en la base de datos.
     */
    @Test
    void createProduct_cuandoProductNoExiste_debeRetornarProductoDTO(){

        // Preparamos los datos de entrada de los objetos falsos
        // ARRANGE
        Long id = 1L;
        CreateProductDTO dto = new CreateProductDTO(
                "Laptop Dell",
                "Laptop para Desarrollo",
                new BigDecimal("2000000.00"),
                10
                );

        Product productoEntidad = new Product();
        productoEntidad.setId(id);
        productoEntidad.setName("Laptop Dell");

        ProductDTO productoDTOEsperado = new ProductDTO();
        productoDTOEsperado.setId(id);
        productoDTOEsperado.setName("Laptop Dell");

        // Le decimos a los objetos falsos que deben retornar
        when(productRepository.existsByName("Laptop Dell")).thenReturn(false);
        when(productMapper.toEntity(dto)).thenReturn(productoEntidad);
        when(productRepository.save(productoEntidad)).thenReturn(productoEntidad);
        when(productMapper.toDto(productoEntidad)).thenReturn(productoDTOEsperado);

        // ACT - Ejecutamos el método real
        ProductDTO resultado = productService.createProduct(dto);

        // ASSERT - verificamos que el resultado es el esperado
        assertNotNull(resultado);
        assertEquals("Laptop Dell", resultado.getName());
        assertEquals(id, resultado.getId());
    }
    /**
     * Verifica que {@code createProduct} lanza {@link ConflictoException}
     * cuando ya existe un producto con el mismo nombre, y que el repositorio
     * nunca ejecuta el método {@code save}.
     */
    @Test
    void createProduct_cuandoProductYaExiste_debeLanzarConflictoException(){

        // Preparamos los datos de entrada de los objetos falsos
        CreateProductDTO dto = new CreateProductDTO(
                "Laptop Dell",
                "Laptop para Desarrollo",
                new BigDecimal("2000000.00"),
                10
        );
        when(productRepository.existsByName("Laptop Dell")).thenReturn(true);
        assertThrows(ConflictoException.class, () -> {
            productService.createProduct(dto);
        });
        // Verifica que el save nunca se guardó
        verify(productRepository, never()).save(any());
    }

    /**
     * Verifica que {@code findById} retorna el {@link ProductDTO} correcto
     * cuando el producto con el ID dado existe en la base de datos.
     */
    @Test
    void findById_cuandoProductExiste_retornaProductoDTO(){

        // ARRANGE
        Long id = 1L;
        Product productoEntidad = new Product();
        productoEntidad.setId(id);
        productoEntidad.setName("Laptop Dell");

        ProductDTO productoDTOEsperado = new ProductDTO();
        productoDTOEsperado.setId(id);
        productoDTOEsperado.setName("Laptop Dell");

        when(productRepository.findById(id)).thenReturn(Optional.of(productoEntidad));
        when(productMapper.toDto(productoEntidad)).thenReturn(productoDTOEsperado);

        // ACT
        ProductDTO resultado = productService.findById(id);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals("Laptop Dell", resultado.getName());
    }

    /**
     * Verifica que {@code findById} lanza {@link RecursoNoEncontradoException}
     * cuando no existe ningún producto con el ID proporcionado.
     */
    @Test
    void findById_cuandoProductNoExiste_debeLanzarException(){

        // ARRANQUE
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RecursoNoEncontradoException.class, () -> {
            productService.findById(99L);
        });
    }

    /**
     * Verifica que {@code updateProduct} actualiza correctamente el producto
     * y retorna él {@link ProductDTO} con los nuevos datos cuando el producto existe
     * y el nuevo nombre no está en uso.
     */
    @Test
    void updateProduct_cuandoProductoExiste_debeRetornarProductDTO(){
        // ARRANGE
        Long id = 1L;
        UpdateProductDTO dto = new UpdateProductDTO();
        dto.setName("Laptop Dell Pro");
        dto.setPrice(new BigDecimal("2000000.00"));

        Product productoExistente = new Product();
        productoExistente.setId(id);
        productoExistente.setName("Laptop Dell");

        ProductDTO productoEsperado = new ProductDTO();
        productoEsperado.setId(id);
        productoEsperado.setName("Laptop Dell Pro");

        when(productRepository.findById(id)).thenReturn(Optional.of(productoExistente));
        when(productRepository.existsByName("Laptop Dell Pro")).thenReturn(false);
        when(productRepository.save(productoExistente)).thenReturn(productoExistente);
        when(productMapper.toDto(productoExistente)).thenReturn(productoEsperado);

        // ACT
        ProductDTO resultado = productService.updateProduct(id, dto);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("Laptop Dell Pro", resultado.getName());
    }

    /**
     * Verifica que {@code updateProduct} lanza {@link RecursoNoEncontradoException}
     * cuando el producto con el ID dado no existe en la base de datos.
     */
    @Test
    void updateProduct_cuandoNoExiste_debeLanzarException(){
        // ARRANGE
        Long id = 1L;
        UpdateProductDTO dto = new UpdateProductDTO();
        dto.setName("Laptop Dell Pro");

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        // ASSERT
        assertThrows(RecursoNoEncontradoException.class, () -> {
            productService.updateProduct(id, dto);
        });
    }

    /**
     * Verifica que {@code deleteById} elimina correctamente el producto
     * cuando existe en la base de datos, confirmando que el repositorio
     * ejecutó el método {@code deleteById}.
     */
    @Test
    void deleteById_cuandoProductExiste_eliminaProducto(){
        // ARRANGE
        Long id = 1L;

        when(productRepository.existsById(id)).thenReturn(true);

        // ACT
        productService.deleteById(id);

        // ASSERT
        verify(productRepository).deleteById(id);
    }

    /**
     * Verifica que {@code deleteById} lanza {@link RecursoNoEncontradoException}
     * cuando el producto no existe, y que el repositorio nunca ejecuta
     * el método {@code deleteById}.
     */
    @Test
    void deleteById_cuandoProductNoExiste_lanzaException(){
        // ARRANGE
        Long id = 99L;
        when(productRepository.existsById(id)).thenReturn(false);

        // ACT & ASSERT
        assertThrows(RecursoNoEncontradoException.class, () -> {
            productService.deleteById(id);
        });
        verify(productRepository, never()).deleteById(id);
    }

    /**
     * Verifica que {@code findAllProducts} retorna una página de productos
     * correctamente estructurada en un {@link PageResponseDTO}, validando
     * el contenido y los metadatos de paginación.
     */
    @Test
    void findAllProduct_debeRetornarListaProduct(){
        // ARRANGE
        Long id = 1L;

        Product productEntidad = new Product();
        productEntidad.setId(id);
        productEntidad.setName("Laptop Dell");

        ProductDTO productEsperado = new ProductDTO();
        productEsperado.setId(id);
        productEsperado.setName("Laptop Dell");

        List<Product> productos = List.of(productEntidad);

        // simulamos una página de Spring con nuestros productos falsos
        Page<Product> paginaFalsa = new PageImpl<>(productos, PageRequest.of(0, 10, Sort.by("name")), 1);

        when(productRepository.findAll(PageRequest.of(0, 10, Sort.by("name")))).thenReturn(paginaFalsa);
        when(productMapper.toDto(productEntidad)).thenReturn(productEsperado);

        // ACT
        PageResponseDTO<ProductDTO> resultado = productService.findAllProducts(0, 10, "name");

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals("Laptop Dell", resultado.content().get(0).getName());
        assertEquals(1, resultado.totalElements());
    }

}
