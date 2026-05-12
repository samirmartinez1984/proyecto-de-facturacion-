package com.sistema.facturacion2.service;

import com.sistema.facturacion2.dto.request.CreateRoleDTO;
import com.sistema.facturacion2.dto.request.CreateUserDTO;
import com.sistema.facturacion2.dto.request.LoginRequestDTO;
import com.sistema.facturacion2.dto.request.UpdateUserDTO;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de negocio para la gestión de usuarios y autenticación.
 *
 * <p>Centraliza los casos de uso de usuario: registro, login, consulta,
 * actualización y eliminación. Orquesta repositorios, mappers, seguridad JWT
 * y validaciones de dominio conforme al contrato de capas del proyecto.</p>
 *
 * <p>Reglas de negocio implementadas:</p>
 * <ul>
 *   <li>Username y email deben ser únicos en el sistema.</li>
 *   <li>Al registrar, se asigna automáticamente el rol {@code ROLE_USER}.</li>
 *   <li>La contraseña se almacena siempre con hash Bcrypt.</li>
 *   <li>El token JWT se genera tras registro o login exitoso.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * <p>Flujo:</p>
     * <ol>
     *   <li>Valida unicidad de {@code username} y {@code email}.</li>
     *   <li>Asigna el rol por defecto {@code ROLE_USER}.</li>
     *   <li>Codifica la contraseña con Bcrypt.</li>
     *   <li>Persiste el usuario y genera un token JWT.</li>
     * </ol>
     *
     * @param dto datos del nuevo usuario
     * @return respuesta con token JWT e información del usuario registrado
     * @throws ConflictoException           si username o email ya existen
     * @throws RecursoNoEncontradoException si el rol ROLE_USER no está en BD
     */
    @Transactional
    public AuthResponseDTO register(CreateUserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new ConflictoException(
                    "El username '" + dto.getUsername() + "' ya está en uso");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictoException(
                    "El email '" + dto.getEmail() + "' ya está en uso");
        }

        Role roleUser = roleRepository.findByName(RoleConstants.ROLE_USER)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Rol ROLE_USER no configurado en el sistema"));

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEnabled(true);
        user.getRoles().add(roleUser);

        User saved = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(saved.getUsername());
        String token = jwtTokenProvider.generateToken(userDetails);

        return new AuthResponseDTO(token, userMapper.toDto(saved));
    }

    /**
     * Auténtica un usuario y devuelve un token JWT si las credenciales son válidas.
     *
     * <p>Delega la verificación de credenciales al {@link AuthenticationManager}
     * de Spring Security (que internamente usa Bcrypt). Si las credenciales son
     * incorrectas, Spring lanza {@link BadCredentialsException} que se transforma
     * en {@link NoAutorizadoException} para mantener el contrato de errores del API.</p>
     *
     * @param dto credenciales de acceso (username + password)
     * @return respuesta con token JWT e información del usuario autenticado
     * @throws NoAutorizadoException        si las credenciales son incorrectas
     * @throws RecursoNoEncontradoException si el usuario no existe en BD
     */
    public AuthResponseDTO login(LoginRequestDTO dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getUsername(), dto.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new NoAutorizadoException("Credenciales inválidas");
        }

        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario no encontrado: " + dto.getUsername()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtTokenProvider.generateToken(userDetails);

        return new AuthResponseDTO(token, userMapper.toDto(user));
    }

    /**
     * Obtiene la lista completa de usuarios registrados en el sistema.
     *
     * @return lista de DTOs de usuario (sin contraseñas)
     */
    @Transactional(readOnly = true)
    public List<UserDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    /**
     * Busca un usuario por su identificador único.
     *
     * @param id identificador del usuario
     * @return DTO del usuario encontrado
     * @throws RecursoNoEncontradoException si no existe usuario con ese id
     */
    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario con id " + id + " no encontrado"));
        return userMapper.toDto(user);
    }

    /**
     * Actualiza los datos editables de un usuario existente.
     *
     * <p>Solo se permiten modificar {@code email} y {@code enabled}.
     * Se valida que el nuevo email no esté ya en uso por otro usuario.</p>
     *
     * @param id  identificador del usuario a actualizar
     * @param dto datos de actualización
     * @return DTO del usuario con los datos actualizados
     * @throws RecursoNoEncontradoException si no existe usuario con ese id
     * @throws ConflictoException           si el nuevo email ya lo usa otro usuario
     */
    @Transactional
    public UserDTO update(Long id, UpdateUserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario con id " + id + " no encontrado"));

        if (dto.getEmail() != null
                && !user.getEmail().equals(dto.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictoException(
                    "El email '" + dto.getEmail() + "' ya está en uso");
        }

        userMapper.updateEntityFromDto(dto, user);
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    /**
     * Elimina un usuario del sistema por su identificador.
     *
     * @param id identificador del usuario a eliminar
     * @throws RecursoNoEncontradoException si no existe usuario con ese id
     */
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RecursoNoEncontradoException(
                    "Usuario con id " + id + " no encontrado");
        }
        userRepository.deleteById(id);
    }
    
    /**
     * Asigna un rol existente a un usuario del sistema.
     *
     * <p>Flujo:</p>
     * <ol>
     *   <li>Busca el usuario por su {@code userId}.</li>
     *   <li>Busca el rol por su nombre ({@code roleName}).</li>
     *   <li>Válida que el usuario no tenga ya ese rol asignado.</li>
     *   <li>Agrega el rol al usuario y persiste el cambio.</li>
     * </ol>
     *
     * @param userId   identificador del usuario al que se asignará el rol
     * @param roleName nombre del rol a asignar (ej.: {@code ROLE_ADMIN})
     * @return DTO del usuario actualizado con el nuevo rol
     * @throws RecursoNoEncontradoException si el usuario o el rol no existen en BD
     * @throws ConflictoException           si el usuario ya tiene ese rol asignado
     */
    @Transactional
    public UserDTO assingRole(Long userId, String roleName){
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario con ID " + userId + " no encontrado"));
        
        Role rol = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "rol " + roleName + " encontrado"));
        
        if (user.getRoles().contains(rol)){
            throw new ConflictoException(
                    "El usuario ya tiene asignado el rol " + roleName);
        }
        // Asignar el rol al usuario
        user.getRoles().add(rol);
        
        // Guardar y retornal
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }
}
