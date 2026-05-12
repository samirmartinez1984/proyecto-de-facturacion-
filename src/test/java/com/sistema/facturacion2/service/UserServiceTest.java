package com.sistema.facturacion2.service;

import com.sistema.facturacion2.dto.request.CreateUserDTO;
import com.sistema.facturacion2.dto.request.LoginRequestDTO;
import com.sistema.facturacion2.dto.response.AuthResponseDTO;
import com.sistema.facturacion2.dto.response.UserDTO;
import com.sistema.facturacion2.exception.ConflictoException;
import com.sistema.facturacion2.exception.NoAutorizadoException;
import com.sistema.facturacion2.exception.RecursoNoEncontradoException;
import com.sistema.facturacion2.mapper.UserMapper;
import com.sistema.facturacion2.model.auth.Role;
import com.sistema.facturacion2.model.auth.RoleConstants;
import com.sistema.facturacion2.model.auth.User;
import com.sistema.facturacion2.repository.RoleRepository;
import com.sistema.facturacion2.repository.UserRepository;
import com.sistema.facturacion2.security.JwtTokenProvider;
import com.sistema.facturacion2.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private UserService userService;

    // ===================== REGISTER =====================

    /**
     * Verifica que {@code register} registra exitosamente un usuario nuevo,
     * genera un token JWT y retorna un {@link AuthResponseDTO} con los datos del usuario.
     */
    @Test
    void register_cuandoUsuarioNoExiste_debeRetornarAuthResponseDTO() {
        // ARRANGE
        Long id = 1L;
        CreateUserDTO dto = new CreateUserDTO(
                "Juanito", "pass123", "juan@mail.com");

        Role roleUser = new Role();
        roleUser.setName(RoleConstants.ROLE_USER);

        User usuarioEntidad = new User();
        usuarioEntidad.setId(id);
        usuarioEntidad.setUsername("Juanito");
        usuarioEntidad.setRoles(new HashSet<>());

        UserDTO userDTOEsperado = new UserDTO();
        userDTOEsperado.setId(id);
        userDTOEsperado.setUsername("Juanito");

        UserDetails userDetailsMock = mock(UserDetails.class);

        when(userRepository.existsByUsername("Juanito")).thenReturn(false);
        when(userRepository.existsByEmail("juan@mail.com")).thenReturn(false);
        when(roleRepository.findByName(RoleConstants.ROLE_USER)).thenReturn(Optional.of(roleUser));
        when(userMapper.toEntity(dto)).thenReturn(usuarioEntidad);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashedPass");
        when(userRepository.save(usuarioEntidad)).thenReturn(usuarioEntidad);
        when(userDetailsService.loadUserByUsername("Juanito")).thenReturn(userDetailsMock);
        when(jwtTokenProvider.generateToken(userDetailsMock)).thenReturn("jwt-token-fake");
        when(userMapper.toDto(usuarioEntidad)).thenReturn(userDTOEsperado);

        // ACT
        AuthResponseDTO resultado = userService.register(dto);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("jwt-token-fake", resultado.getToken());
        assertEquals("Juanito", resultado.getUser().getUsername());
    }

    /**
     * Verifica que {@code register} lanza {@link ConflictoException}
     * cuando el username ya está registrado en el sistema.
     */
    @Test
    void register_cuandoUsernameYaExiste_debeLanzarConflictoException() {
        // ARRANGE
        CreateUserDTO dto = new CreateUserDTO("Juanito", "pass123", "juan@mail.com");
        when(userRepository.existsByUsername("Juanito")).thenReturn(true);

        // ACT & ASSERT
        assertThrows(ConflictoException.class, () -> userService.register(dto));
        verify(userRepository, never()).save(any());
    }

    /**
     * Verifica que {@code register} lanza {@link ConflictoException}
     * cuando el email ya está registrado en el sistema.
     */
    @Test
    void register_cuandoEmailYaExiste_debeLanzarConflictoException() {
        // ARRANGE
        CreateUserDTO dto = new CreateUserDTO("Juanito", "pass123", "juan@mail.com");
        when(userRepository.existsByUsername("Juanito")).thenReturn(false);
        when(userRepository.existsByEmail("juan@mail.com")).thenReturn(true);

        // ACT & ASSERT
        assertThrows(ConflictoException.class, () -> userService.register(dto));
        verify(userRepository, never()).save(any());
    }

    // ===================== LOGIN =====================

    /**
     * Verifica que {@code login} retorna un {@link AuthResponseDTO} con token JWT
     * cuando las credenciales son correctas.
     */
    @Test
    void login_cuandoCredencialesSonCorrectas_debeRetornarAuthResponseDTO() {
        // ARRANGE
        LoginRequestDTO dto = new LoginRequestDTO("Juanito", "pass123");

        User usuarioEntidad = new User();
        usuarioEntidad.setUsername("Juanito");

        UserDTO userDTOEsperado = new UserDTO();
        userDTOEsperado.setUsername("Juanito");

        UserDetails userDetailsMock = mock(UserDetails.class);

        when(userRepository.findByUsername("Juanito")).thenReturn(Optional.of(usuarioEntidad));
        when(userDetailsService.loadUserByUsername("Juanito")).thenReturn(userDetailsMock);
        when(jwtTokenProvider.generateToken(userDetailsMock)).thenReturn("jwt-token-fake");
        when(userMapper.toDto(usuarioEntidad)).thenReturn(userDTOEsperado);

        // ACT
        AuthResponseDTO resultado = userService.login(dto);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("jwt-token-fake", resultado.getToken());
        assertEquals("Juanito", resultado.getUser().getUsername());
    }

    /**
     * Verifica que {@code login} lanza {@link NoAutorizadoException}
     * cuando las credenciales son incorrectas.
     */
    @Test
    void login_cuandoCredencialesInvalidas_debeLanzarNoAutorizadoException() {
        // ARRANGE
        LoginRequestDTO dto = new LoginRequestDTO("Juanito", "wrongPass");
        doThrow(BadCredentialsException.class)
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // ACT & ASSERT
        assertThrows(NoAutorizadoException.class, () -> userService.login(dto));
    }

    // ===================== FIND ALL =====================

    /**
     * Verifica que {@code findAll} retorna la lista completa de usuarios como DTOs.
     */
    @Test
    void findAll_debeRetornarListaDeUsuarios() {
        // ARRANGE
        Long id = 1L;
        User usuario = new User();
        usuario.setId(id);
        usuario.setUsername("Juanito");

        UserDTO userDTO = new UserDTO();
        userDTO.setId(id);
        userDTO.setUsername("Juanito");

        when(userRepository.findAll()).thenReturn(List.of(usuario));
        when(userMapper.toDto(usuario)).thenReturn(userDTO);

        // ACT
        List<UserDTO> resultado = userService.findAll();

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Juanito", resultado.get(0).getUsername());
    }

    // ===================== FIND BY ID =====================

    /**
     * Verifica que {@code findById} retorna el {@link UserDTO} correcto
     * cuando el usuario con el ID dado existe.
     */
    @Test
    void findById_cuandoUsuarioExiste_debeRetornarUserDTO() {
        // ARRANGE
        Long id = 1L;
        User usuario = new User();
        usuario.setId(id);
        usuario.setUsername("Juanito");

        UserDTO userDTOEsperado = new UserDTO();
        userDTOEsperado.setId(id);
        userDTOEsperado.setUsername("Juanito");

        when(userRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(userMapper.toDto(usuario)).thenReturn(userDTOEsperado);

        // ACT
        UserDTO resultado = userService.findById(id);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals("Juanito", resultado.getUsername());
    }

    /**
     * Verifica que {@code findById} lanza {@link RecursoNoEncontradoException}
     * cuando no existe un usuario con el ID proporcionado.
     */
    @Test
    void findById_cuandoUsuarioNoExiste_debeLanzarException() {
        // ARRANGE
        Long id = 99L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RecursoNoEncontradoException.class, () -> userService.findById(99L));
    }

    // ===================== DELETE =====================

    /**
     * Verifica que {@code delete} elimina correctamente el usuario
     * cuando existe en la base de datos.
     */
    @Test
    void delete_cuandoUsuarioExiste_debeEliminarUsuario() {
        // ARRANGE
        Long id = 1L;
        when(userRepository.existsById(id)).thenReturn(true);

        // ACT
        userService.delete(id);

        // ASSERT
        verify(userRepository).deleteById(id);
    }

    /**
     * Verifica que {@code delete} lanza {@link RecursoNoEncontradoException}
     * cuando el usuario no existe, y que el repositorio nunca ejecuta {@code deleteById}.
     */
    @Test
    void delete_cuandoUsuarioNoExiste_debeLanzarException() {
        // ARRANGE
        Long id = 99L;
        when(userRepository.existsById(id)).thenReturn(false);

        // ACT & ASSERT
        assertThrows(RecursoNoEncontradoException.class, () -> userService.delete(id));
        verify(userRepository, never()).deleteById(id);
    }

    // ===================== ASSIGN ROLE =====================

    /**
     * Verifica que {@code assingRole} asigna correctamente un rol a un usuario
     * cuando tanto el usuario como el rol existen y el usuario no tiene ese rol aún.
     */
    @Test
    void assingRole_cuandoUsuarioYRolExisten_debeAsignarRol() {
        // ARRANGE
        Long userId = 1L;
        String roleName = "ROLE_ADMIN";

        Role rol = new Role();
        rol.setName(roleName);

        User usuario = new User();
        usuario.setId(userId);
        usuario.setUsername("Juanito");
        usuario.setRoles(new HashSet<>());

        UserDTO userDTOEsperado = new UserDTO();
        userDTOEsperado.setId(userId);
        userDTOEsperado.setUsername("Juanito");

        when(userRepository.findById(userId)).thenReturn(Optional.of(usuario));
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(rol));
        when(userRepository.save(usuario)).thenReturn(usuario);
        when(userMapper.toDto(usuario)).thenReturn(userDTOEsperado);

        // ACT
        UserDTO resultado = userService.assingRole(userId, roleName);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("Juanito", resultado.getUsername());
        verify(userRepository).save(usuario);
    }

    /**
     * Verifica que {@code assingRole} lanza {@link ConflictoException}
     * cuando el usuario ya tiene asignado el rol que se intenta agregar.
     */
    @Test
    void assingRole_cuandoUsuarioYaTieneElRol_debeLanzarConflictoException() {
        // ARRANGE
        Long userId = 1L;
        String roleName = "ROLE_ADMIN";

        Role rol = new Role();
        rol.setName(roleName);

        User usuario = new User();
        usuario.setId(userId);
        usuario.setRoles(new HashSet<>(Set.of(rol)));

        when(userRepository.findById(userId)).thenReturn(Optional.of(usuario));
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(rol));

        // ACT & ASSERT
        assertThrows(ConflictoException.class, () -> userService.assingRole(userId, roleName));
        verify(userRepository, never()).save(any());
    }
}
