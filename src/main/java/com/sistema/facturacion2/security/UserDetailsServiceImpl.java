package com.sistema.facturacion2.security;

import com.sistema.facturacion2.model.auth.User;
import com.sistema.facturacion2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de {@link UserDetailsService} para la integración con Spring Security.
 *
 * <p>Carga los datos del usuario desde la base de datos y construye el objeto
 * {@link UserDetails} que Spring Security usa internamente para autenticación
 * y autorización. Este servicio es invocado automáticamente por él
 * {@link org.springframework.security.authentication.AuthenticationManager}
 * durante el proceso de login.</p>
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carga el usuario por su nombre de usuario y lo convierte al contrato
     * de Spring Security ({@link UserDetails}).
     *
     * <p>Los roles almacenados como {@code "ROLE_ADMIN"}, {@code "ROLE_USER"}
     * se convierten en {@link SimpleGrantedAuthority} para que Spring Security
     * pueda evaluarlos en las reglas de autorización.</p>
     *
     * @param username nombre de usuario a buscar en la base de datos
     * @return detalles del usuario listos para la cadena de seguridad
     * @throws UsernameNotFoundException si no existe un usuario con ese username
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + username));

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(!Boolean.TRUE.equals(user.getEnabled()))
                .accountExpired(false)
                .credentialsExpired(false)
                .accountLocked(false)
                .build();
    }
}

