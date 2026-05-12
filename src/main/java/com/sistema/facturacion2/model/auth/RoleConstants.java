package com.sistema.facturacion2.model.auth;

/**
 * Clase utilitaria que centraliza los nombres estándar de roles del sistema.
 *
 * <p>Su propósito es evitar valores literales repetidos, mantener consistencia
 * entre capas y respetar la convención {@code ROLE_*} definida para seguridad.</p>
 */
public final class RoleConstants {
    
    /**
     * Rol con privilegios administrativos.
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /**
     * Rol base para usuarios operativos o estándar.
     */
    public static final String ROLE_USER = "ROLE_USER";
    
    /**
     * Constructor privado para impedir la instantiation de la clase utilitaria.
     */
    private RoleConstants() {
        // Clase de utilidades - no instanciar
    }
}
