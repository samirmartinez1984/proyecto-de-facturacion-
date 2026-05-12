package com.sistema.facturacion2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.facturacion2.dto.response.UserDTO;
import com.sistema.facturacion2.exception.ConflictoException;
import com.sistema.facturacion2.exception.GlobalExceptionHandler;
import com.sistema.facturacion2.exception.RecursoNoEncontradoException;
import com.sistema.facturacion2.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests unitarios para {@link AdminController}.
 * <p>
 * Verifica el comportamiento del endpoint de asignación de roles
 * usando {@link MockMvc} en modo standalone, con el {@link UserService} mockeado.
 * </p>
 * Cubre los siguientes casos:
 * <ul>
 *   <li>Asignación exitosa de rol — {@code 200 OK}</li>
 *   <li>Usuario no encontrado — {@code 404 Not Found}</li>
 *   <li>Rol ya asignado — {@code 409 Conflict}</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ===================== ASSIGN ROLE =====================

    /**
     * Verifica que {@code PUT /admin/user/{id}/role} retorna {@code 200 OK}
     * con el {@link UserDTO} actualizado cuando el usuario existe y el rol es válido.
     */
    @Test
    void assingRole_cuandoUsuarioExisteYRolValido_retorna200ConUserDTO() throws Exception {
        // ARRANGE
        Long id = 1L;

        UserDTO userDTO = new UserDTO();
        userDTO.setId(id);
        userDTO.setUsername("juanito");

        when(userService.assingRole(eq(id), any())).thenReturn(userDTO);

        // ACT & ASSERT
        mockMvc.perform(put("/admin/user/{id}/role", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"ROLE_ADMIN\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.username").value("juanito"));
    }

    /**
     * Verifica que {@code PUT /admin/user/{id}/role} retorna {@code 404 Not Found}
     * cuando el usuario con el ID dado no existe en el sistema.
     */
    @Test
    void assingRole_cuandoUsuarioNoExiste_retorna404() throws Exception {
        // ARRANGE
        Long id = 99L;

        when(userService.assingRole(eq(id), any()))
                .thenThrow(new RecursoNoEncontradoException("Usuario no encontrado"));

        // ACT & ASSERT
        mockMvc.perform(put("/admin/user/{id}/role", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"ROLE_ADMIN\""))
                .andExpect(status().isNotFound());
    }

    /**
     * Verifica que {@code PUT /admin/user/{id}/role} retorna {@code 409 Conflict}
     * cuando el usuario ya tiene el rol que se intenta asignar.
     */
    @Test
    void assingRole_cuandoRolYaAsignado_retorna409() throws Exception {
        // ARRANGE
        Long id = 1L;

        when(userService.assingRole(eq(id), any()))
                .thenThrow(new ConflictoException("El usuario ya tiene ese rol"));

        // ACT & ASSERT
        mockMvc.perform(put("/admin/user/{id}/role", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"ROLE_ADMIN\""))
                .andExpect(status().isConflict());
    }
}

