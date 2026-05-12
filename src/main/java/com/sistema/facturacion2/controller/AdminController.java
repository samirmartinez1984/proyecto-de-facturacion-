package com.sistema.facturacion2.controller;

import com.sistema.facturacion2.dto.response.UserDTO;
import com.sistema.facturacion2.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones administrativas del sistema.
 *
 * <p>Expone endpoints protegidos que solo pueden ser consumidos por usuarios
 * con rol {@code ROLE_ADMIN}. Delega la lógica de negocio al {@link com.sistema.facturacion2.service.UserService}.</p>
 *
 * <p>Base URL: {@code /api/admin}</p>
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    /**
     * Asigna un rol a un usuario existente en el sistema.
     *
     * <p>Permite al administrador elevar los permisos de un usuario
     * asignándole un rol adicional (ej: promover a {@code ROLE_ADMIN}).</p>
     *
     * @param id       identificador del usuario al que se asignará el rol
     * @param roleName nombre del rol a asignar en el cuerpo de la petición (ej: {@code ROLE_ADMIN})
     * @return {@code 200 OK} con el DTO del usuario actualizado
     * @throws com.sistema.facturacion2.exception.RecursoNoEncontradoException si el usuario o el rol no existen
     * @throws com.sistema.facturacion2.exception.ConflictoException           si el usuario ya tiene ese rol
     */
    @PutMapping("/user/{id}/role")
    public ResponseEntity<UserDTO> assingRole(@PathVariable Long id, @RequestBody String roleName){
        return ResponseEntity.ok(userService.assingRole(id, roleName));
    }

}
