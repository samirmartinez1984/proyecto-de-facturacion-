package com.sistema.facturacion2.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de seguridad que intercepta cada request HTTP y válida el token JWT.
 *
 * <p>Se ejecuta una sola vez por petición (extiende {@link OncePerRequestFilter}).
 * Su responsabilidad es extraer el token del header {@code Authorization},
 * validarlo y, si es correcto, establecer la autenticación en el
 * {@link SecurityContextHolder} para que Spring Security permita el acceso.</p>
 *
 * <p>Flujo:</p>
 * <ol>
 *   <li>Lee el header {@code Authorization: Bearer &lt;token&gt;}</li>
 *   <li>Si no hay token o no empieza por "Bearer", deja pasar sin autenticar</li>
 *   <li>Extrae el username del token usando {@link JwtTokenProvider}</li>
 *   <li>Carga el usuario desde {@link UserDetailsServiceImpl}</li>
 *   <li>Si el token es válido, registra la autenticación en el contexto</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Lógica principal del filtro: extrae, válida y aplica la autenticación JWT.
     *
     * @param request     petición HTTP entrante
     * @param response    respuesta HTTP
     * @param filterChain cadena de filtros a continuar
     * @throws ServletException ante error de servlet
     * @throws IOException      ante error de I/O
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Si no hay header o no es Bearer, continúa sin autenticar (rutas públicas)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            String username = jwtTokenProvider.getUsernameFromToken(token);

            // Solo autenticar si tenemos username y no hay autenticación previa en el contexto
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtTokenProvider.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            // Token inválido, expirado o malformado: se deja sin autenticación
            // El request continuará y Spring Security devolverá 401 si el endpoint lo requiere
            logger.warn("No se pudo validar el token JWT: " + ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}

