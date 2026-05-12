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
import com.sistema.facturacion2.model.InvoiceStatus;
import com.sistema.facturacion2.model.auth.User;
import com.sistema.facturacion2.repository.InvoiceRepository;
import com.sistema.facturacion2.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InvoiceServiceTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private UserRepository userRepository;
    @Mock private InvoiceMapper invoiceMapper;

    @InjectMocks
    private InvoiceService invoiceService;

    /**
     * Configura un usuario autenticado real en el {@link SecurityContextHolder}
     * antes de cada test que dependa del contexto de seguridad.
     */
    @BeforeEach
    void configurarUsuarioAutenticado() {
        Authentication auth = new UsernamePasswordAuthenticationToken("Juanito", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * Limpia el contexto de seguridad después de cada test para evitar
     * que un test afecte al siguiente.
     */
    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    // ===================== CREATE INVOICE =====================

    /**
     * Verifica que {@code createInvoice} crea exitosamente una factura en estado
     * {@code DRAFT} para el usuario autenticado y retorna un {@link InvoiceDetailDTO}.
     */
    @Test
    void createInvoice_cuandoUsuarioExiste_debeRetornarInvoiceDetailDTO() {
        // ARRANGE
        Long id = 1L;
        CreateInvoiceItemDTO itemDTO = new CreateInvoiceItemDTO("Laptop Dell", 2, new BigDecimal("2000000.00"));
        CreateInvoiceDTO dto = new CreateInvoiceDTO("Factura de prueba", List.of(itemDTO));

        User usuario = new User();
        usuario.setId(id);
        usuario.setUsername("Juanito");

        Invoice invoiceGuardada = new Invoice();
        invoiceGuardada.setId(id);
        invoiceGuardada.setStatus(InvoiceStatus.DRAFT);
        invoiceGuardada.setItems(new ArrayList<>());

        InvoiceDetailDTO detallEsperado = new InvoiceDetailDTO();
        detallEsperado.setId(id);
        detallEsperado.setStatus(InvoiceStatus.DRAFT);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(usuario));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoiceGuardada);
        when(invoiceMapper.toDetailDto(invoiceGuardada)).thenReturn(detallEsperado);

        // ACT
        InvoiceDetailDTO resultado = invoiceService.createInvoice(dto);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(InvoiceStatus.DRAFT, resultado.getStatus());
        verify(invoiceRepository).save(any(Invoice.class));
    }

    /**
     * Verifica que {@code createInvoice} lanza {@link RecursoNoEncontradoException}
     * cuando el usuario autenticado no existe en la base de datos.
     */
    @Test
    void createInvoice_cuandoUsuarioNoExiste_debeLanzarException() {
        // ARRANGE
        CreateInvoiceItemDTO itemDTO = new CreateInvoiceItemDTO("Laptop Dell", 1, new BigDecimal("1000.00"));
        CreateInvoiceDTO dto = new CreateInvoiceDTO("Factura", List.of(itemDTO));

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RecursoNoEncontradoException.class, () -> invoiceService.createInvoice(dto));
        verify(invoiceRepository, never()).save(any());
    }

    // ===================== UPDATE INVOICE =====================

    /**
     * Verifica que {@code updateInvoice} actualiza correctamente una factura
     * que no está cancelada y retorna el {@link InvoiceDetailDTO} actualizado.
     */
    @Test
    void updateInvoice_cuandoFacturaExisteYNoEstaCancelada_debeRetornarInvoiceDetailDTO() {
        // ARRANGE
        Long id = 1L;
        UpdateInvoiceDTO dto = new UpdateInvoiceDTO();
        dto.setDescription("Descripción actualizada");

        Invoice facturaExistente = new Invoice();
        facturaExistente.setId(id);
        facturaExistente.setStatus(InvoiceStatus.DRAFT);
        facturaExistente.setItems(new ArrayList<>());

        InvoiceDetailDTO detallEsperado = new InvoiceDetailDTO();
        detallEsperado.setId(id);

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(facturaExistente));
        when(invoiceRepository.save(facturaExistente)).thenReturn(facturaExistente);
        when(invoiceMapper.toDetailDto(facturaExistente)).thenReturn(detallEsperado);

        // ACT
        InvoiceDetailDTO resultado = invoiceService.updateInvoice(id, dto);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        verify(invoiceRepository).save(facturaExistente);
    }

    /**
     * Verifica que {@code updateInvoice} lanza {@link ConflictoException}
     * cuando la factura ya está en estado {@code CANCELLED}.
     */
    @Test
    void updateInvoice_cuandoFacturaEstaCancelada_debeLanzarConflictoException() {
        // ARRANGE
        Long id = 1L;
        Invoice facturaCancelada = new Invoice();
        facturaCancelada.setId(id);
        facturaCancelada.setStatus(InvoiceStatus.CANCELLED);

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(facturaCancelada));

        // ACT & ASSERT
        assertThrows(ConflictoException.class, () -> invoiceService.updateInvoice(id, new UpdateInvoiceDTO()));
        verify(invoiceRepository, never()).save(any());
    }

    /**
     * Verifica que {@code updateInvoice} lanza {@link RecursoNoEncontradoException}
     * cuando no existe una factura con el ID indicado.
     */
    @Test
    void updateInvoice_cuandoFacturaNoExiste_debeLanzarException() {
        // ARRANGE
        Long id = 99L;
        when(invoiceRepository.findById(id)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RecursoNoEncontradoException.class,
                () -> invoiceService.updateInvoice(id, new UpdateInvoiceDTO()));
    }

    // ===================== FIND ALL INVOICE =====================

    /**
     * Verifica que {@code findAllInvoice} retorna una página de facturas
     * correctamente estructurada en un {@link PageResponseDTO}.
     */
    @Test
    void findAllInvoice_debeRetornarPaginaDeFacturas() {
        // ARRANGE
        Long id = 1L;
        Invoice factura = new Invoice();
        factura.setId(id);

        InvoiceDTO invoiceDTO = new InvoiceDTO();
        invoiceDTO.setId(id);

        Page<Invoice> paginaFalsa = new PageImpl<>(
                List.of(factura), PageRequest.of(
                        0, 10, Sort.by("id")), 1);

        when(invoiceRepository.findAll(PageRequest.of(
                0, 10, Sort.by("id")))).thenReturn(paginaFalsa);
        when(invoiceMapper.toDto(factura)).thenReturn(invoiceDTO);

        // ACT
        PageResponseDTO<InvoiceDTO> resultado = invoiceService.findAllInvoice(0, 10, "id");

        // ASSERT
        assertNotNull(resultado);
        assertEquals(id, resultado.content().size());
        assertEquals(id, resultado.content().get(0).getId());
        assertEquals(id, resultado.totalElements());
    }

    // ===================== FIND BY ID =====================

    /**
     * Verifica que {@code findById} retorna el {@link InvoiceDetailDTO} correcto
     * cuando la factura con el ID dado existe.
     */
    @Test
    void findById_cuandoFacturaExiste_debeRetornarInvoiceDetailDTO() {
        // ARRANGE
        Long id = 1L;
        Invoice factura = new Invoice();
        factura.setId(id);

        InvoiceDetailDTO detallEsperado = new InvoiceDetailDTO();
        detallEsperado.setId(id);

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(factura));
        when(invoiceMapper.toDetailDto(factura)).thenReturn(detallEsperado);

        // ACT
        InvoiceDetailDTO resultado = invoiceService.findById(id);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
    }

    /**
     * Verifica que {@code findById} lanza {@link RecursoNoEncontradoException}
     * cuando no existe ninguna factura con el ID proporcionado.
     */
    @Test
    void findById_cuandoFacturaNoExiste_debeLanzarException() {
        // ARRANGE
        Long id = 99L;
        when(invoiceRepository.findById(id)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RecursoNoEncontradoException.class, () -> invoiceService.findById(id));
    }

    // ===================== FIND MY INVOICES =====================

    /**
     * Verifica que {@code findMyInvoices} retorna la lista de facturas
     * pertenecientes al usuario autenticado en el contexto de seguridad.
     */
    @Test
    void findMyInvoices_debeRetornarFacturasDelUsuarioAutenticado() {
        // ARRANGE
        Long id = 1L;
        Invoice factura = new Invoice();
        factura.setId(id);

        InvoiceDTO invoiceDTO = new InvoiceDTO();
        invoiceDTO.setId(id);

        when(invoiceRepository.findByUsernameWithUser(anyString())).thenReturn(List.of(factura));
        when(invoiceMapper.toDto(factura)).thenReturn(invoiceDTO);

        // ACT
        List<InvoiceDTO> resultado = invoiceService.findMyInvoices();

        // ASSERT
        assertNotNull(resultado);
        assertEquals(id, resultado.size());
        assertEquals(id, resultado.get(0).getId());
    }

    // ===================== CANCEL INVOICE =====================

    /**
     * Verifica que {@code cancelInvoice} cambia el estado de la factura
     * a {@code CANCELLED} y retorna el {@link InvoiceDetailDTO} actualizado.
     */
    @Test
    void cancelInvoice_cuandoFacturaEstaActiva_debeCancelarYRetornarDetailDTO() {
        // ARRANGE
        Long id = 1L;
        Invoice factura = new Invoice();
        factura.setId(id);
        factura.setStatus(InvoiceStatus.DRAFT);

        InvoiceDetailDTO detallEsperado = new InvoiceDetailDTO();
        detallEsperado.setId(id);
        detallEsperado.setStatus(InvoiceStatus.CANCELLED);

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(factura));
        when(invoiceRepository.save(factura)).thenReturn(factura);
        when(invoiceMapper.toDetailDto(factura)).thenReturn(detallEsperado);

        // ACT
        InvoiceDetailDTO resultado = invoiceService.cancelInvoice(id);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(InvoiceStatus.CANCELLED, resultado.getStatus());
        verify(invoiceRepository).save(factura);
    }

    /**
     * Verifica que {@code cancelInvoice} lanza {@link ConflictoException}
     * cuando la factura ya está en estado {@code CANCELLED}.
     */
    @Test
    void cancelInvoice_cuandoFacturaYaEstaCancelada_debeLanzarConflictoException() {
        // ARRANGE
        Long id = 1L;
        Invoice factura = new Invoice();
        factura.setId(id);
        factura.setStatus(InvoiceStatus.CANCELLED);

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(factura));

        // ACT & ASSERT
        assertThrows(ConflictoException.class, () -> invoiceService.cancelInvoice(id));
        verify(invoiceRepository, never()).save(any());
    }

    /**
     * Verifica que {@code cancelInvoice} lanza {@link ConflictoException}
     * cuando la factura está en estado {@code PAID} y no puede ser cancelada.
     */
    @Test
    void cancelInvoice_cuandoFacturaEstaPagada_debeLanzarConflictoException() {
        // ARRANGE
        Long id = 1L;
        Invoice factura = new Invoice();
        factura.setId(id);
        factura.setStatus(InvoiceStatus.PAID);

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(factura));

        // ACT & ASSERT
        assertThrows(ConflictoException.class, () -> invoiceService.cancelInvoice(id));
        verify(invoiceRepository, never()).save(any());
    }

    /**
     * Verifica que {@code cancelInvoice} lanza {@link RecursoNoEncontradoException}
     * cuando no existe una factura con el ID indicado.
     */
    @Test
    void cancelInvoice_cuandoFacturaNoExiste_debeLanzarException() {
        // ARRANGE
        Long id = 99L;
        when(invoiceRepository.findById(id)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RecursoNoEncontradoException.class, () -> invoiceService.cancelInvoice(id));
    }
}
