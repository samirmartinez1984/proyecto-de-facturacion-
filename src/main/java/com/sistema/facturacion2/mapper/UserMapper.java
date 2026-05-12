package com.sistema.facturacion2.mapper;

import com.sistema.facturacion2.dto.request.CreateUserDTO;
import com.sistema.facturacion2.dto.request.UpdateUserDTO;
import com.sistema.facturacion2.dto.response.RoleDTO;
import com.sistema.facturacion2.dto.response.UserDTO;
import com.sistema.facturacion2.model.auth.Role;
import com.sistema.facturacion2.model.auth.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper responsable de convertir entre la entidad {@link User}
 * y los DTO de entrada/salida de usuario.
 */
@Component
public class UserMapper {

    /**
     * Convertir una entidad {@link User} a {@link UserDTO}.
     *
     * @param entity entidad de usuario a transformar
     * @return DTO de respuesta o {@code null} si la entidad es {@code null}
     */
    public UserDTO toDto(User entity){
        if (entity == null){
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setEnabled(entity.getEnabled());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setRoles(toRoleDtoSet(entity.getRoles()));
        return dto;
    }

    /**
     * Convertir un {@link CreateUserDTO} a entidad {@link User}.
     *
     * @param userDTO DTO de creación
     * @return entidad creada o {@code null} si el DTO es {@code null}
     */
    public User toEntity(CreateUserDTO userDTO){
        if (userDTO == null){
            return null;
        }
        User entity = new User();
        entity.setUsername(userDTO.getUsername());
        entity.setEmail(userDTO.getEmail());
        entity.setPassword(userDTO.getPassword());
        return entity;
    }

    /**
     * Aplicar cambios de {@link UpdateUserDTO} sobre una entidad existente.
     *
     * @param dto DTO con datos de actualización
     * @param entity entidad destinó a modificar
     */
    public void updateEntityFromDto(UpdateUserDTO dto, User entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setEmail(dto.getEmail());
        entity.setEnabled(dto.getEnabled());
    }

    /**
     * Convertir el conjunto de roles de la entidad a DTOs de rol.
     *
     * @param roles conjunto de roles de la entidad
     * @return conjunto de {@link RoleDTO}; vacío si no hay roles
     */
    private Set<RoleDTO> toRoleDtoSet(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptySet();
        }

        return roles.stream()
                .map(this::toRoleDto)
                .collect(Collectors.toSet());
    }

    /**
     * Convertir una entidad {@link Role} a {@link RoleDTO}.
     *
     * @param role entidad de rol
     * @return DTO de rol o {@code null} si la entidad es {@code null}
     */
    private RoleDTO toRoleDto(Role role) {
        if (role == null) {
            return null;
        }

        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setCreatedAt(role.getCreatedAt());
        return dto;
    }

}
