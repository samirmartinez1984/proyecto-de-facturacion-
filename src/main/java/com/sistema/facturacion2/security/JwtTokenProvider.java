package com.sistema.facturacion2.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Componente responsable de la generación, firma y validación de tokens JWT.
 *
 * <p>Utiliza el algoritmo {@code HS512} con una clave secreta configurable.
 * El ciclo de vida del token incluye: generación al autenticar, lectura del
 * subject (username) y verificación de vigencia en cada request.</p>
 *
 * <p>Configuración requerida en {@code application.properties}:</p>
 * <pre>
 *   app.jwt.secret=&lt;clave de al menos 64 caracteres&gt;
 *   app.jwt.expiration=86400000
 * </pre>
 */
@Component
public class JwtTokenProvider {

    /**
     * Clave secreta usada para firmar y verificar los tokens.
     * Debe tener al menos 64 caracteres para HS512.
     */
    @Value("${app.jwt.secret}")
    private String secret;

    /**
     * Tiempo de vida del token en milisegundos (por defecto 24 horas).
     */
    @Value("${app.jwt.expiration}")
    private long expiration;

    /**
     * Genera un token JWT firmado con HS512 para el usuario autenticado.
     *
     * @param userDetails detalles del usuario cargados por Spring Security
     * @return token JWT compacto listo para enviar al cliente
     */
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Extrae el username (subject) del token JWT.
     *
     * @param token token JWT a analizar
     * @return username almacenado en el subject del token
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Verifica que el token sea válido: pertenece al usuario y no está expirado.
     *
     * @param token       token JWT a validar
     * @param userDetails detalles del usuario contra los que se verifica
     * @return {@code true} si el token es válido, {@code false} en caso contrario
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = getUsernameFromToken(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Comprueba si el token ha superado su fecha de expiración.
     *
     * @param token token JWT a verificar
     * @return {@code true} si el token está expirado
     */
    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    /**
     * Parsea y retorna el payload (Claims) del token.
     *
     * @param token token JWT firmado
     * @return claims del token
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Construye la {@link SecretKey} derivada del secreto configurado.
     *
     * @return clave HMAC-SHA lista para firmar/verificar
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}

