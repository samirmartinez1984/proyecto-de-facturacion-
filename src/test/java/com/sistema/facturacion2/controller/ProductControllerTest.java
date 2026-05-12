package com.sistema.facturacion2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.facturacion2.dto.request.CreateProductDTO;
import com.sistema.facturacion2.dto.response.ProductDTO;
import com.sistema.facturacion2.exception.ConflictoException;
import com.sistema.facturacion2.exception.GlobalExceptionHandler;
import com.sistema.facturacion2.service.ProductService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import com.sistema.facturacion2.dto.request.UpdateProductDTO;
import com.sistema.facturacion2.dto.response.PageResponseDTO;
import com.sistema.facturacion2.exception.RecursoNoEncontradoException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Verifica que {@code POST /products} retorna {@code 200 OK}
     * con el {@link ProductDTO} creado cuando los datos son válidos.
     */
    @Test
    void createProduct_cuandoDatosValidos_retorna200ConProductoDTO() throws Exception {
        // ARRANGE
        Long id = 1L;

        CreateProductDTO dto = new CreateProductDTO(
                "Laptop Dell", "pc para desarrollo",
                new BigDecimal("2000000"), 10);

        ProductDTO productoEsperado = new ProductDTO();
        productoEsperado.setId(id);
        productoEsperado.setName("Laptop Dell");

        when(productService.createProduct(any())).thenReturn(productoEsperado);

        String json = objectMapper.writeValueAsString(dto);

        // ACT & ASSERT
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Laptop Dell"));
    }
    
    //Caso negativo
    @SneakyThrows
    @Test
    void createProduct_cuandoDatosInvalidos_lanzaException(){
        CreateProductDTO dto = new CreateProductDTO(
                "Laptop Dell", 
                "pc para desarrollo",
                new BigDecimal("2000000"), 
                10);
        
        when(productService.createProduct(any()))
                .thenThrow(new ConflictoException("producto ya existe"));
        
        String json = objectMapper.writeValueAsString(dto);
        
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status()
                .isConflict());
    }

    // ===================== GET /products =====================

    /**
     * Verifica que {@code GET /products} retorna {@code 200 OK}
     * con una página de productos correctamente estructurada.
     */
    @Test
    void findAllProducts_debeRetornar200ConPaginaDeProductos() throws Exception {
        // ARRANGE
        ProductDTO producto = new ProductDTO();
        producto.setId(1L);
        producto.setName("Laptop Dell");

        PageResponseDTO<ProductDTO> paginaEsperada = new PageResponseDTO<>(
                List.of(producto), 0, 10, 1L, 1, true);

        when(productService.findAllProducts(0, 10, "id")).thenReturn(paginaEsperada);

        // ACT & ASSERT
        mockMvc.perform(get("/products")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Laptop Dell"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // ===================== GET /products/{id} =====================

    /**
     * Verifica que {@code GET /products/{id}} retorna {@code 200 OK}
     * con el producto encontrado cuando el ID existe.
     */
    @Test
    void findById_cuandoProductoExiste_retorna200ConProductoDTO() throws Exception {
        // ARRANGE
        Long id = 1L;
        ProductDTO productoEsperado = new ProductDTO();
        productoEsperado.setId(id);
        productoEsperado.setName("Laptop Dell");

        when(productService.findById(id)).thenReturn(productoEsperado);

        // ACT & ASSERT
        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Laptop Dell"));
    }

    /**
     * Verifica que {@code GET /products/{id}} retorna {@code 404 Not Found}
     * cuando el producto no existe.
     */
    @Test
    void findById_cuandoProductoNoExiste_retorna404() throws Exception {
        // ARRANGE
        Long id = 99L;
        when(productService.findById(id))
                .thenThrow(new RecursoNoEncontradoException("Producto no encontrado"));

        // ACT & ASSERT
        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    // ===================== PUT /products/{id} =====================

    /**
     * Verifica que {@code PUT /products/{id}} retorna {@code 200 OK}
     * con el producto actualizado cuando los datos son válidos.
     */
    @Test
    void updateProduct_cuandoDatosValidos_retorna200ConProductoDTO() throws Exception {
        // ARRANGE
        Long id = 1L;
        UpdateProductDTO dto = new UpdateProductDTO();
        dto.setName("Laptop Dell Pro");
        dto.setPrice(new BigDecimal("2500000"));

        ProductDTO productoEsperado = new ProductDTO();
        productoEsperado.setId(id);
        productoEsperado.setName("Laptop Dell Pro");

        when(productService.updateProduct(eq(id), any())).thenReturn(productoEsperado);

        String json = objectMapper.writeValueAsString(dto);

        // ACT & ASSERT
        mockMvc.perform(put("/products/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Laptop Dell Pro"));
    }

    /**
     * Verifica que {@code PUT /products/{id}} retorna {@code 404 Not Found}
     * cuando el producto a actualizar no existe.
     */
    @Test
    void updateProduct_cuandoProductoNoExiste_retorna404() throws Exception {
        // ARRANGE
        Long id = 99L;
        UpdateProductDTO dto = new UpdateProductDTO();
        dto.setName("Laptop Dell Pro");

        when(productService.updateProduct(eq(id), any()))
                .thenThrow(new RecursoNoEncontradoException("Producto no encontrado"));

        String json = objectMapper.writeValueAsString(dto);

        // ACT & ASSERT
        mockMvc.perform(put("/products/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    // ===================== DELETE /products/{id} =====================

    /**
     * Verifica que {@code DELETE /products/{id}} retorna {@code 204 No Content}
     * cuando el producto existe y es eliminado correctamente.
     */
    @Test
    void deleteProduct_cuandoProductoExiste_retorna204() throws Exception {
        // ARRANGE
        Long id = 1L;
        doNothing().when(productService).deleteById(id);

        // ACT & ASSERT
        mockMvc.perform(delete("/products/{id}", id))
                .andExpect(status().isNoContent());
    }

    /**
     * Verifica que {@code DELETE /products/{id}} retorna {@code 404 Not Found}
     * cuando el producto no existe.
     */
    @Test
    void deleteProduct_cuandoProductoNoExiste_retorna404() throws Exception {
        // ARRANGE
        Long id = 99L;
        doThrow(new RecursoNoEncontradoException("Producto no encontrado"))
                .when(productService).deleteById(id);

        // ACT & ASSERT
        mockMvc.perform(delete("/products/{id}", id))
                .andExpect(status().isNotFound());
    }
}
