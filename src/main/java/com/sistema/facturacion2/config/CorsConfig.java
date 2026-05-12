package com.sistema.facturacion2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración global de CORS para permitir peticiones desde el frontend Vue.
 *
 * <p>Habilita el acceso desde los orígenes del frontend de desarrollo,
 * define los métodos HTTP y headers permitidos, y expone el header
 * {@code Authorization} para que Vue pueda leer el token JWT.</p>
 *
 * <p>Si el puerto de tu frontend Vue es diferente a los configurados,
 * agrégalo en {@code setAllowedOrigins}.</p>
 */
@Configuration
public class CorsConfig {

    /**
     * Define la política CORS aplicada a todos los endpoints ({@code /**}).
     *
     * @return fuente de configuración CORS registrada globalmente
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // ✅ Origen permitido — frontend Vue corriendo en Vite
        config.setAllowedOrigins(List.of(
                "http://localhost:5173"
        ));

        // ✅ Métodos HTTP que puede usar el frontend
        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"   // necesario para el preflight del navegador
        ));

        // ✅ Headers que puede enviar el frontend (incluye Authorization para JWT)
        config.setAllowedHeaders(List.of("*"));

        // ✅ Permite que Vue lea el header Authorization de la respuesta
        config.setExposedHeaders(List.of("Authorization"));

        // ✅ Permite envío de credenciales (cookies / tokens)
        config.setAllowCredentials(true);

        // Aplica esta configuración a TODOS los endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}

