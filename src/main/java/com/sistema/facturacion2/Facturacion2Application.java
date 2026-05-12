package com.sistema.facturacion2;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

/**
 * <h1>sistema de Facturación - Aplicación Principal</h1>
 * 
 * <p>
 * Este es un sistema profesional de facturación desarrollado con Spring Boot 4.0.3 y Java 21.
 * El sistema permite gestionar usuarios, productos, facturas e items de factura con 
 * autenticación y autorización basada en roles.
 * </p>
 * 
 * <h2>Características principales:</h2>
 * <ul>
 *   <li><strong>Gestión de Usuarios:</strong> Registro, autenticación y autorización</li>
 *   <li><strong>Sistema de Roles:</strong> ROLE_ADMIN, ROLE_USER con permisos diferenciados</li>
 *   <li><strong>Catálogo de Productos:</strong> Gestión completa de inventario</li>
 *   <li><strong>Facturación:</strong> Creación y gestión de facturas con items</li>
 *   <li><strong>Seguridad:</strong> Spring Security con autenticación JWT</li>
 *   <li><strong>Base de Datos:</strong> MySQL con migraciones Flyway</li>
 * </ul>
 * 
 * <h2>Tecnologías utilizadas:</h2>
 * <ul>
 *   <li>Spring Boot 4.0.3</li>
 *   <li>Spring Data JPA</li>
 *   <li>Spring Security</li>
 *   <li>MySQL 8.0+</li>
 *   <li>Flyway para migraciones</li>
 *   <li>Lombok para reducir código boilerplate</li>
 *   <li>Maven como gestor de dependencias</li>
 * </ul>
 * 
 * <h2>Estructura del proyecto:</h2>
 * <pre>
 * Com.sistema.facturacion2
 * </pre>
 * 
 * @author Sistema de Facturación Team
 * @version 0.0.1-SNAPSHOT
 * @since 2026-03-13
 */
@SpringBootApplication
public class Facturacion2Application {

	/**
	 * Fija la zona horaria de la JVM a America/Mexico_City al iniciar la aplicación.
	 *
	 * <p>Garantiza que todos los {@code LocalDateTime.now()} del sistema
	 * capturen la hora local de México, independientemente de la configuración
	 * del sistema operativo donde corre el servidor.</p>
	 */
	@PostConstruct
	void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
	}

	/**
	 * Método principal que inicia la aplicación Spring Boot.
	 * 
	 * <p>
	 * Configura y arranca el contexto de Spring, inicializa la base de datos
	 * con Flyway y expone los endpoints REST en el puerto configurado (8080 por defecto).
	 * </p>
	 * 
	 * @param args argumentos de línea de comandos pasados a la aplicación
	 */
	public static void main(String[] args) {
		SpringApplication.run(Facturacion2Application.class, args);
	}

}
