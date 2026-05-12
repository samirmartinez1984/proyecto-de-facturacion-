/**
 * <h1>Sistema de Facturación - Paquete Principal</h1>
 * 
 * <p>
 * Este paquete contiene la implementación completa de un sistema profesional de facturación
 * desarrollado con Spring Boot 4.0.3, Java 21 y tecnologías modernas de desarrollo empresarial.
 * </p>
 * 
 * <h2>Arquitectura del Sistema</h2>
 * 
 * <p>El sistema está diseñado siguiendo los principios de arquitectura limpia y 
 * patrón de capas, proporcionando separación de responsabilidades y alta mantenibilidad.</p>
 * 
 * <h3>Estructura de Paquetes:</h3>
 * <dl>
 *   <dt><strong>{@link com.sistema.facturacion2.model}</strong></dt>
 *   <dd>Contiene las entidades JPA del dominio del negocio</dd>
 *   
 *   <dt><strong>{@link com.sistema.facturacion2.model.auth}</strong></dt>
 *   <dd>Entidades relacionadas con autenticación y autorización (User, Role)</dd>
 *   
 *   <dt><strong>{@link com.sistema.facturacion2.repository}</strong></dt>
 *   <dd>Repositorios Spring Data JPA para acceso a datos optimizado</dd>
 * </dl>
 * 
 * <h2>Entidades del Dominio</h2>
 * 
 * <h3>Gestión de Usuarios y Seguridad:</h3>
 * <ul>
 *   <li>{@link com.sistema.facturacion2.model.auth.User} - Usuario del sistema</li>
 *   <li>{@link com.sistema.facturacion2.model.auth.Role} - Roles y permisos</li>
 * </ul>
 * 
 * <h3>Gestión de Productos:</h3>
 * <ul>
 *   <li>{@link com.sistema.facturacion2.model.Product} - Catálogo de productos</li>
 * </ul>
 * 
 * <h3>Sistema de Facturación:</h3>
 * <ul>
 *   <li>{@link com.sistema.facturacion2.model.Invoice} - Facturas del sistema</li>
 *   <li>{@link com.sistema.facturacion2.model.InvoiceItem} - Items individuales de factura</li>
 *   <li>{@link com.sistema.facturacion2.model.InvoiceStatus} - Estados de factura (DRAFT, PAID, CANCELLED)</li>
 * </ul>
 * 
 * <h2>Acceso a Datos</h2>
 * 
 * <p>Todos los repositorios extienden {@link org.springframework.data.jpa.repository.JpaRepository}
 * y proporcionan métodos optimizados para consultas específicas del dominio:</p>
 * 
 * <ul>
 *   <li>{@link com.sistema.facturacion2.repository.UserRepository} - Gestión de usuarios</li>
 *   <li>{@link com.sistema.facturacion2.repository.RoleRepository} - Gestión de roles</li>
 *   <li>{@link com.sistema.facturacion2.repository.ProductRepository} - Gestión de productos</li>
 *   <li>{@link com.sistema.facturacion2.repository.InvoiceRepository} - Gestión de facturas</li>
 *   <li>{@link com.sistema.facturacion2.repository.InvoiceItemRepository} - Gestión de items</li>
 * </ul>
 * 
 * <h2>Tecnologías Utilizadas</h2>
 * 
 * <ul>
 *   <li><strong>Spring Boot 4.0.3:</strong> Framework principal de aplicación</li>
 *   <li><strong>Spring Data JPA:</strong> Abstracción de acceso a datos</li>
 *   <li><strong>Spring Security:</strong> Autenticación y autorización</li>
 *   <li><strong>Flyway:</strong> Control de versiones de base de datos</li>
 *   <li><strong>MySQL:</strong> Base de datos relacional</li>
 *   <li><strong>Lombok:</strong> Reducción de código boilerplate</li>
 *   <li><strong>Maven:</strong> Gestión de dependencias y construcción</li>
 * </ul>
 * 
 * <h2>Configuración e Inicio</h2>
 * 
 * <p>Para iniciar el sistema:</p>
 * <pre>
 * mvn spring-boot:run
 * </pre>
 * 
 * <p>El sistema estará disponible en: {@code http://localhost:8080}</p>
 * 
 * @author Sistema de Facturación Team
 * @version 0.0.1-SNAPSHOT
 * @since 2026-03-13
 */
package com.sistema.facturacion2;
