package com.sistema.facturacion2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.facturacion2.dto.request.CreateUserDTO;
import com.sistema.facturacion2.dto.request.LoginRequestDTO;
import com.sistema.facturacion2.dto.response.AuthResponseDTO;
import com.sistema.facturacion2.dto.response.UserDTO;
import com.sistema.facturacion2.exception.ConflictoException;
import com.sistema.facturacion2.exception.GlobalExceptionHandler;
import com.sistema.facturacion2.exception.NoAutorizadoException;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests unitarios para {@link AuthenticationController}.
 * <p>
 * Verifica el comportamiento de los endpoints de registro e inicio de sesión
 * usando {@link MockMvc} en modo standalone, con el {@link UserService} mockeado.
 * </p>
 * Cubre los siguientes casos:
 * <ul>
 *   <li>Registro exitoso — {@code 200 OK}</li>
 *   <li>Registro con usuario duplicado — {@code 409 Conflict}</li>
 *   <li>Login exitoso — {@code 200 OK}</li>
 *   <li>Login con credenciales inválidas — {@code 401 Unauthorized}</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ===================== REGISTER =====================

    /**
     * Verifica que {@code POST /user/register} retorna {@code 200 OK}
     * con el token JWT y los datos del usuario cuando el registro es exitoso.
     */
    @Test
    void register_cuandoDatosValidos_retorna200ConAuthResponse() throws Exception {
        // ARRANGE
        CreateUserDTO dto = new CreateUserDTO("juanito", "password123", "juanito@email.com");

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("juanito");

        AuthResponseDTO respuestaEsperada = new AuthResponseDTO("jwt-token-falso", userDTO);

        when(userService.register(any())).thenReturn(respuestaEsperada);

        String json = objectMapper.writeValueAsString(dto);

        // ACT & ASSERT
        mockMvc.perform(post("/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-falso"))
                .andExpect(jsonPath("$.user.username").value("juanito"));
    }

    /**
     * Verifica que {@code POST /user/register} retorna {@code 409 Conflict}
     * cuando ya existe un usuario con el mismo username o email.
     */
    @Test
    void register_cuandoUsuarioDuplicado_retorna409() throws Exception {
        // ARRANGE
        CreateUserDTO dto = new CreateUserDTO("juanito", "password123", "juanito@email.com");

        when(userService.register(any()))
                .thenThrow(new ConflictoException("El usuario ya existe"));

        String json = objectMapper.writeValueAsString(dto);

        // ACT & ASSERT
        mockMvc.perform(post("/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());
    }

    // ===================== LOGIN =====================

    /**
     * Verifica que {@code POST /user/login} retorna {@code 200 OK}
     * con el token JWT cuando las credenciales son correctas.
     */
    @Test
    void login_cuandoCredencialesValidas_retorna200ConToken() throws Exception {
        // ARRANGE
        LoginRequestDTO dto = new LoginRequestDTO("juanito", "password123");

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("juanito");

        AuthResponseDTO respuestaEsperada = new AuthResponseDTO("jwt-token-falso", userDTO);

        when(userService.login(any())).thenReturn(respuestaEsperada);

        String json = objectMapper.writeValueAsString(dto);

        // ACT & ASSERT
        mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-falso"))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    /**
     * Verifica que {@code POST /user/login} retorna {@code 401 Unauthorized}
     * cuando las credenciales son incorrectas.
     */
    @Test
    void login_cuandoCredencialesInvalidas_retorna401() throws Exception {
        // ARRANGE
        LoginRequestDTO dto = new LoginRequestDTO("juanito", "password_incorrecta");

        when(userService.login(any()))
                .thenThrow(new NoAutorizadoException("Credenciales inválidas"));

        String json = objectMapper.writeValueAsString(dto);

        // ACT & ASSERT
        mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized());
    }
}
