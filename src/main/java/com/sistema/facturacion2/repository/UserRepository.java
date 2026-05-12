package com.sistema.facturacion2.repository;

import com.sistema.facturacion2.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * <h1>Repositorio para gestión de usuarios y autenticación</h1>
 * 
 * <p>
 * Este repositorio proporciona métodos para realizar operaciones CRUD sobre la entidad {@link User},
 * con funcionalidades específicas para autenticación, registro y gestión de usuarios.
 * </p>
 * 
 * <h2>Funcionalidades principales:</h2>
 * <ul>
 *   <li>Búsqueda de usuarios por username y email</li>
 *   <li>Validación de unicidad para registro</li>
 *   <li>Carga optimizada de usuarios con roles para Spring Security</li>
 * </ul>
 * 
 * <p><strong>Nota:</strong> Todos los métodos básicos de JpaRepository están disponibles automáticamente:
 * {@code save()}, {@code findById()}, {@code findAll()}, {@code deleteById()}, {@code count()}, etc.</p>
 * 
 * @see User
 * @see com.sistema.facturacion2.model.auth.Role
 * @author Sistema de Facturación Team
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Busca un usuario por su nombre de usuario único.
     * 
     * <p>
     * Este método es utilizado principalmente para procesos de autenticación
     * donde se requiere validar las credenciales del usuario.
     * </p>
     * 
     * @param username el nombre de usuario único a buscar (no puede ser null)
     * @return Optional conteniendo el usuario si existe, empty() si no se encuentra
     * 
     * @example
     * <pre>
     * Optional&lt;User&gt; user = userRepository.findByUsername("admin");
     * if (user.isPresent()) {
     *     System.out.println("Usuario encontrado: " + user.get().getUsername());
     * }
     * </pre>
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Busca un usuario por su dirección de email única.
     * 
     * <p>
     * Útil para funcionalidades como "recuperar contraseña" o validaciones
     * durante el proceso de registro.
     * </p>
     * 
     * @param email la dirección de email única a buscar (no puede ser null)
     * @return Optional conteniendo el usuario si existe, empty() si no se encuentra
     * 
     * @example
     * <pre>
     * Optional&lt;User&gt; user = userRepository.findByEmail("admin@sistema.com");
     * </pre>
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Verifica si ya existe un usuario con el username especificado.
     * 
     * <p>
     * Método de validación utilizado durante el proceso de registro para
     * garantizar la unicidad del nombre de usuario.
     * </p>
     * 
     * @param username el nombre de usuario a verificar (no puede ser null)
     * @return {@code true} si el username ya está en uso, {@code false} en caso contrario
     * 
     * @example
     * <pre>
     * if (userRepository.existsByUsername("nuevoUser")) {
     *     throw new RuntimeException("El username ya está en uso");
     * }
     * </pre>
     */
    boolean existsByUsername(String username);
    
    /**
     * Verifica si ya existe un usuario con el email especificado.
     * 
     * <p>
     * Método de validación utilizado durante el proceso de registro para
     * garantizar la unicidad del email.
     * </p>
     * 
     * @param email la dirección de email a verificar (no puede ser null)
     * @return {@code true} si el email ya está en uso, {@code false} en caso contrario
     * 
     * @example
     * <pre>
     * if (userRepository.existsByEmail("nuevo@email.com")) {
     *     throw new RuntimeException("El email ya está registrado");
     * }
     * </pre>
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca un usuario con sus roles cargados de forma optimizada.
     * 
     * <p>
     * <strong>Optimización importante:</strong> Este método utiliza LEFT JOIN FETCH para evitar
     * el problema N+1 de Hibernate, cargando el usuario y todos sus roles en una sola consulta SQL.
     * </p>
     * 
     * <p>
     * Es el método recomendado para Spring Security, ya que los roles son requeridos
     * para determinar los permisos del usuario autenticado.
     * </p>
     * 
     * @param username el nombre de usuario a buscar con sus roles (no puede ser null)
     * @return Optional conteniendo el usuario con roles cargados, empty() si no se encuentra
     * 
     * @example
     * <pre>
     * Optional&lt;User&gt; userWithRoles = userRepository.findByUsernameWithRoles("admin");
     * if (userWithRoles.isPresent()) {
     *     Set&lt;Role&gt; roles = userWithRoles.get().getRoles(); // NO ejecuta nueva query
     *     roles.forEach(role -&gt; System.out.println("Rol: " + role.getName()));
     * }
     * </pre>
     * 
     * @see com.sistema.facturacion2.model.auth.Role
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);
}
