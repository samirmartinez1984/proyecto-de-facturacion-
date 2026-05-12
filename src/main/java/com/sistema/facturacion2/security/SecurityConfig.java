package com.sistema.facturacion2.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Configuración central de Spring Security para el sistema de facturación.
 *
 * <p>Define la cadena de filtros de seguridad con política stateless (JWT),
 * deshabilita CSRF (API REST), declara las rutas públicas y protegidas, y
 * registra él {@link JwtAuthenticationFilter} antes del filtro estándar de
 * autenticación de Spring Security.</p>
 *
 * <p>Rutas públicas (sin token):</p>
 * <ul>
 *   <li>{@code POST /user/register} — registro de nuevos usuarios</li>
 *   <li>{@code POST /user/login} — inicio de sesión</li>
 * </ul>
 *
 * <p>Rutas protegidas (solo {@code ROLE_ADMIN}):</p>
 * <ul>
 *   <li>Todos los endpoints bajo {@code /admin/**}</li>
 * </ul>
 *
 * <p>Rutas protegidas (cualquier usuario autenticado):</p>
 * <ul>
 *   <li>Todos los demás endpoints</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * Define y configura la cadena principal de filtros de seguridad HTTP.
     *
     * @param http objeto de configuración de Spring Security
     * @return cadena de filtros construida y lista
     * @throws Exception si ocurre un error de configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Habilita CORS usando la configuración definida en CorsConfig
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Deshabilita CSRF: no aplica en APIs REST stateless
                .csrf(AbstractHttpConfigurer::disable)

                // Sin sesiones HTTP: cada request debe traer su propio token
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Definición de reglas de autorización por endpoint
                .authorizeHttpRequests(auth -> auth
                        // Permite el acceso a Swagger y la documentación sin autenticación
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        
                        // Rutas públicas: registro y login no requieren token
                        .requestMatchers(HttpMethod.POST, "/user/register", "/user/login").permitAll()


                        // Rutas de administración: solo accesibles con rol ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Rutas de facturación: requieren autenticación (rol se valida con @PreAuthorize)
                        .requestMatchers("/invoices/**").authenticated()

                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated())
                
                // Proveedor de autenticación con BCrypt y UserDetailsService
                .authenticationProvider(authenticationProvider())
                
                // El filtro JWT se ejecuta antes del filtro estándar de credenciales
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Configura el proveedor de autenticación que conecta Spring Security
     * con la base de datos y BCrypt para verificar contraseñas.
     *
     * @return proveedor de autenticación DAO configurado
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Expone el {@link AuthenticationManager} como bean para ser inyectado
     * en {@code UserService} y usarlo en el proceso de login.
     *
     * @param config configuración de autenticación de Spring
     * @return gestor de autenticación configurado
     * @throws Exception si ocurre un error al obtenerlo
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Bean de codificación de contraseñas con BCrypt (factor de coste por defecto: 10).
     *
     * @return codificador Bcrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


