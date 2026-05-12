package com.sistema.facturacion2.repository;

import com.sistema.facturacion2.model.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * <h1>Repositorio para gestión de roles del sistema</h1>
 * 
 * <p>
 * Este repositorio maneja las operaciones CRUD sobre la entidad {@link Role}.
 * Los roles son utilizados por Spring Security para implementar autorización
 * basada en permisos dentro del sistema de facturación.
 * </p>
 * 
 * <h2>Roles del sistema:</h2>
 * <ul>
 *   <li><strong>ROLE_ADMIN:</strong> Administrador con acceso completo</li>
 *   <li><strong>ROLE_USER:</strong> Usuario estándar con permisos básicos</li>
 * </ul>
 * 
 * <p>Los roles se relacionan con usuarios mediante una tabla intermedia {@code user_roles}
 * implementando una relación Many-to-Many.</p>
 * 
 * @see Role
 * @see com.sistema.facturacion2.model.auth.User
 * @author Sistema de Facturación Team
 * @since 1.0.0
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Busca un rol por su nombre único.
     * 
     * <p>
     * Los nombres de roles siguen la convención de Spring Security:
     * deben comenzar con "ROLE_" seguido del nombre del rol en mayúsculas.
     * </p>
     * 
     * @param name el nombre único del rol (ej: "ROLE_ADMIN", "ROLE_USER")
     * @return Optional conteniendo el rol si existe, empty() si no se encuentra
     * 
     * @example
     * <pre>
     * Optional&lt;Role&gt; adminRole = roleRepository.findByName("ROLE_ADMIN");
     * if (adminRole.isPresent()) {
     *     System.out.println("Rol encontrado: " + adminRole.get().getName());
     * }
     * </pre>
     */
    Optional<Role> findByName(String name);
    
    /**
     * Verifica si ya existe un rol con el nombre especificado.
     * 
     * <p>
     * Método de validación utilizado antes de crear nuevos roles
     * para garantizar la unicidad de los nombres.
     * </p>
     * 
     * @param name el nombre del rol a verificar (no puede ser null)
     * @return {@code true} si el nombre ya está en uso, {@code false} en caso contrario
     * 
     * @example
     * <pre>
     * if (!roleRepository.existsByName("ROLE_MANAGER")) {
     *     Role newRole = new Role();
     *     newRole.setName("ROLE_MANAGER");
     *     roleRepository.save(newRole);
     * }
     * </pre>
     */
    boolean existsByName(String name);
}
