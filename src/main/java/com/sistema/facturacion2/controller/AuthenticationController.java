package com.sistema.facturacion2.controller;

import com.sistema.facturacion2.dto.request.CreateUserDTO;
import com.sistema.facturacion2.dto.request.LoginRequestDTO;
import com.sistema.facturacion2.dto.response.AuthResponseDTO;
import com.sistema.facturacion2.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión de autenticación de usuarios.
 *
 * <p>Expone los endpoints públicos de registro e inicio de sesión.
 * Delega la lógica de negocio al {@link UserService}.</p>
 *
 * <p>Base URL: {@code /api/user}</p>
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * <p>Crea el usuario con rol {@code ROLE_USER} por defecto y
     * retorna un token JWT junto con los datos del usuario creado.</p>
     *
     * @param createUserDTO datos del usuario a registrar
     * @return {@code 200 OK} con el token JWT y la información del usuario
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> registerUser(@RequestBody CreateUserDTO createUserDTO){
        return ResponseEntity.ok(userService.register(createUserDTO));
    }

    /**
     * Auténtica un usuario existente en el sistema.
     *
     * <p>Válida las credenciales del usuario y, si son correctas,
     * retorna un token JWT para su uso en peticiones posteriores.</p>
     *
     * @param loginRequestDTO credenciales del usuario (username y password)
     * @return {@code 200 OK} con el token JWT y la información del usuario autenticado
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginUser(@RequestBody LoginRequestDTO loginRequestDTO){
        return ResponseEntity.ok(userService.login(loginRequestDTO));
    }

}
