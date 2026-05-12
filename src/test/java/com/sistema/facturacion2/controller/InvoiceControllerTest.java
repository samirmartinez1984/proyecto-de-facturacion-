package com.sistema.facturacion2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.facturacion2.dto.request.CreateInvoiceDTO;
import com.sistema.facturacion2.dto.request.CreateInvoiceItemDTO;
import com.sistema.facturacion2.dto.request.UpdateInvoiceDTO;
import com.sistema.facturacion2.dto.response.InvoiceDTO;
import com.sistema.facturacion2.dto.response.InvoiceDetailDTO;
import com.sistema.facturacion2.dto.response.PageResponseDTO;
import com.sistema.facturacion2.exception.ConflictoException;
import com.sistema.facturacion2.exception.GlobalExceptionHandler;
import com.sistema.facturacion2.exception.RecursoNoEncontradoException;
import com.sistema.facturacion2.model.InvoiceStatus;
import com.sistema.facturacion2.service.InvoiceService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests unitarios para {@link InvoiceController}.
 * <p>
 * Verifica el comportamiento HTTP de cada endpoint del módulo de facturación
 * usando {@link MockMvc} en modo standalone, con el {@link InvoiceService} mockeado.
 * </p>
 * Cubre los siguientes casos:
 * <ul>
 *   <li>Crear factura — {@code 201 Created} y {@code 404 Not Found}</li>
 *   <li>Actualizar factura — {@code 200 OK}, {@code 409 Conflict} y {@code 404 Not Found}</li>
 *   <li>Listar facturas paginadas — {@code 200 OK}</li>
 *   <li>Buscar por ID — {@code 200 OK} y {@code 404 Not Found}</li>
 *   <li>Mis facturas — {@code 200 OK}</li>
 *   <li>Cancelar factura — {@code 200 OK} y {@code 409 Conflict}</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
public class InvoiceControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private InvoiceController invoiceController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(invoiceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ===================== CREATE INVOICE =====================

    /**
     * Verifica que {@code POST /invoices} retorna {@code 201 Created}
     * con el detalle de la factura cuando los datos son válidos.
     */
    @Test
    void createInvoice_cuandoDatosValidos_retorna201ConInvoiceDetailDTO() throws Exception {
        // ARRANGE
        Long id = 1L;
        CreateInvoiceItemDTO itemDTO = new CreateInvoiceItemDTO("Laptop Dell", 2, new BigDecimal("2000000.00"));
        CreateInvoiceDTO dto = new CreateInvoiceDTO("Factura de prueba", List.of(itemDTO));

        InvoiceDetailDTO detallEsperado = new InvoiceDetailDTO();
        detallEsperado.setId(id);
        detallEsperado.setStatus(InvoiceStatus.DRAFT);

        when(invoiceService.createInvoice(any())).thenReturn(detallEsperado);

        String json = objectMapper.writeValueAsString(dto);

        // ACT & ASSERT
        mockMvc.perform(post("/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id));
    }

    /**
     * Verifica que {@code POST /invoices} retorna {@code 404 Not Found}
     * cuando el usuario autenticado no existe en la base de datos.
     */
    @Test
    void createInvoice_cuandoUsuarioNoExiste_retorna404() throws Exception {
        // ARRANGE
        CreateInvoiceItemDTO itemDTO = new CreateInvoiceItemDTO("Laptop Dell", 1, new BigDecimal("1000.00"));
        CreateInvoiceDTO dto = new CreateInvoiceDTO("Factura", List.of(itemDTO));

        when(invoiceService.createInvoice(any()))
                .thenThrow(new RecursoNoEncontradoException("Usuario no encontrado"));

        String json = objectMapper.writeValueAsString(dto);

        // ACT & ASSERT
        mockMvc.perform(post("/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    // ===================== UPDATE INVOICE =====================

    /**
     * Verifica que {@code PUT /invoices/{id}} retorna {@code 200 OK}
     * con el detalle actualizado cuando la factura existe y no está cancelada.
     */
    @Test
    void updateInvoice_cuandoFacturaExiste_retorna200ConInvoiceDetailDTO() throws Exception {
        // ARRANGE
        Long id = 1L;
        UpdateInvoiceDTO dto = new UpdateInvoiceDTO();
        dto.setDescription("Descripción actualizada");

        InvoiceDetailDTO detallEsperado = new InvoiceDetailDTO();
        detallEsperado.setId(id);

        when(invoiceService.updateInvoice(eq(id), any())).thenReturn(detallEsperado);

        String json = objectMapper.writeValueAsString(dto);

        // ACT & ASSERT
        mockMvc.perform(put("/invoices/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    /**
     * Verifica que {@code PUT /invoices/{id}} retorna {@code 409 Conflict}
     * cuando la factura ya está cancelada y no puede modificarse.
     */
    @Test
    void updateInvoice_cuandoFacturaCancelada_retorna409() throws Exception {
        // ARRANGE
        Long id = 1L;
        UpdateInvoiceDTO dto = new UpdateInvoiceDTO();

        when(invoiceService.updateInvoice(eq(id), any()))
                .thenThrow(new ConflictoException("No se puede modificar una factura cancelada"));

        String json = objectMapper.writeValueAsString(dto);

        // ACT & ASSERT
        mockMvc.perform(put("/invoices/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());
    }

    /**
     * Verifica que {@code PUT /invoices/{id}} retorna {@code 404 Not Found}
     * cuando no existe una factura con el ID indicado.
     */
    @Test
    void updateInvoice_cuandoFacturaNoExiste_retorna404() throws Exception {
        // ARRANGE
        Long id = 99L;
        UpdateInvoiceDTO dto = new UpdateInvoiceDTO();

        when(invoiceService.updateInvoice(eq(id), any()))
                .thenThrow(new RecursoNoEncontradoException("Factura no encontrada"));

        String json = objectMapper.writeValueAsString(dto);

        // ACT & ASSERT
        mockMvc.perform(put("/invoices/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    // ===================== FIND ALL =====================

    /**
     * Verifica que {@code GET /invoices} retorna {@code 200 OK}
     * con una página de facturas correctamente estructurada.
     */
    @Test
    void findAllInvoice_debeRetornar200ConPaginaDeFacturas() throws Exception {
        // ARRANGE
        Long id = 1L;
        InvoiceDTO invoiceDTO = new InvoiceDTO();
        invoiceDTO.setId(id);

        PageResponseDTO<InvoiceDTO> paginaEsperada = new PageResponseDTO<>(
                List.of(invoiceDTO), 0, 10, 1L, 1, true);

        when(invoiceService.findAllInvoice(0, 10, "id")).thenReturn(paginaEsperada);

        // ACT & ASSERT
        mockMvc.perform(get("/invoices")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));
    }

    // ===================== FIND BY ID =====================

    /**
     * Verifica que {@code GET /invoices/{id}} retorna {@code 200 OK}
     * con el detalle de la factura cuando existe.
     */
    @Test
    void findById_cuandoFacturaExiste_retorna200ConInvoiceDetailDTO() throws Exception {
        // ARRANGE
        Long id = 1L;
        InvoiceDetailDTO detallEsperado = new InvoiceDetailDTO();
        detallEsperado.setId(id);

        when(invoiceService.findById(id)).thenReturn(detallEsperado);

        // ACT & ASSERT
        mockMvc.perform(get("/invoices/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    /**
     * Verifica que {@code GET /invoices/{id}} retorna {@code 404 Not Found}
     * cuando no existe una factura con ese ID.
     */
    @Test
    void findById_cuandoFacturaNoExiste_retorna404() throws Exception {
        // ARRANGE
        Long id = 99L;
        when(invoiceService.findById(id))
                .thenThrow(new RecursoNoEncontradoException("Factura no encontrada"));

        // ACT & ASSERT
        mockMvc.perform(get("/invoices/{id}", id))
                .andExpect(status().isNotFound());
    }

    // ===================== FIND MY INVOICES =====================

    /**
     * Verifica que {@code GET /invoices/my} retorna {@code 200 OK}
     * con la lista de facturas del usuario autenticado.
     */
    @Test
    void findMyInvoices_debeRetornar200ConListaDeFacturas() throws Exception {
        // ARRANGE
        Long id = 1L;
        InvoiceDTO invoiceDTO = new InvoiceDTO();
        invoiceDTO.setId(id);

        when(invoiceService.findMyInvoices()).thenReturn(List.of(invoiceDTO));

        // ACT & ASSERT
        mockMvc.perform(get("/invoices/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
    }

    // ===================== CANCEL INVOICE =====================

    /**
     * Verifica que {@code PATCH /invoices/{id}/cancel} retorna {@code 200 OK}
     * con la factura en estado {@code CANCELLED} cuando la operación es exitosa.
     */
    @Test
    void cancelInvoice_cuandoFacturaActiva_retorna200ConEstadoCancelado() throws Exception {
        // ARRANGE
        Long id = 1L;
        InvoiceDetailDTO detallEsperado = new InvoiceDetailDTO();
        detallEsperado.setId(id);
        detallEsperado.setStatus(InvoiceStatus.CANCELLED);

        when(invoiceService.cancelInvoice(id)).thenReturn(detallEsperado);

        // ACT & ASSERT
        mockMvc.perform(patch("/invoices/{id}/cancel", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    /**
     * Verifica que {@code PATCH /invoices/{id}/cancel} retorna {@code 409 Conflict}
     * cuando la factura ya está cancelada o en estado {@code PAID}.
     */
    @Test
    void cancelInvoice_cuandoFacturaYaCancelada_retorna409() throws Exception {
        // ARRANGE
        Long id = 1L;
        when(invoiceService.cancelInvoice(id))
                .thenThrow(new ConflictoException("La factura ya está cancelada"));

        // ACT & ASSERT
        mockMvc.perform(patch("/invoices/{id}/cancel", id))
                .andExpect(status().isConflict());
    }
}
