# Sistema de Facturación Empresarial

Sistema de gestión de facturas electrónicas construido con Spring Boot y Vue.js. El proyecto cubre el ciclo completo:
autenticación, CRUD de entidades, generación de facturas con sus ítems, documentación de la API y un pipeline de CI/CD que corre en cada push.
Está pensado para escalar: arquitectura en capas clara, migraciones versionadas, logging estructurado listo para producción
y cero consultas N+1.

## Stack

**Backend** — Spring Boot 3, Java 21, JPA/Hibernate, Spring Security, JWT, Flyway, Logback  
**Base de datos** — MySQL 8.0  
**Frontend** — Vue.js 3  
**Testing** — JUnit 5, Mockito, MockMvc  
**Infraestructura** — GitHub Actions, Maven


## Qué tiene de interesante técnicamente

**Entity Graphs para eliminar N+1.** En vez de dejar que Hibernate resuelva las relaciones lazy de manera implícita
(y generar decenas de queries extra), las relaciones críticas se cargan explícitamente:

```java
@EntityGraph(attributePaths = {"user", "items"})
Optional<Invoice> findById(Long id);
```

**Logging JSON listo para producción.** Los logs salen en formato estructurado con Logstash encoder, 
lo que permite indexarlos directamente en cualquier stack de observabilidad (ELK, Grafana Loki, etc.) sin parseo adicional.

**Migraciones con Flyway.** El esquema de base de datos está versionado. Cada cambio es trazable y reproducible en cualquier entorno.

**61 tests, 0 fallos.** Cobertura en controllers, services, repositories, validaciones y seguridad.
El pipeline rechaza cualquier push que rompa un test.

---

## Estructura del proyecto
```
src/main/java/com/sistema/facturacion2/
├── controller/       # Endpoints REST
├── service/          # Lógica de negocio
├── repository/       # Acceso a datos
├── entity/           # Modelos JPA
├── dto/              # Objetos de transferencia
├── mapper/           # Mapeo entity <-> DTO
├── exception/        # Manejo de errores centralizado
└── security/         # JWT + Spring Security
```

Scripts de migración en `src/main/resources/db/migration/`  
Pipeline en `.github/workflows/main.yml`

---

## Modelo de datos

```
Usuario ──< Factura ──< ItemFactura >── Producto
```

Cuatro tablas: `users`, `invoices`, `invoice_items`, `products` (300+ registros de catálogo).

---

## Cómo correrlo

**Requisitos:** Java 21, MySQL 8.0, Maven 3.8+, Node.js 18+

```bash
# Clonar
git clone https://github.com/tu-usuario/facturacion2.git
cd facturacion2

# Configurar BD en src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/facturacion_db
spring.datasource.username=root
spring.datasource.password=tu_password

# Backend
mvn clean install
mvn spring-boot:run

# Frontend
cd frontend
npm install
npm run dev
```

Flyway aplica las migraciones automáticamente al levantar. La app queda en `http://localhost:8080`.

---

## API

Documentación interactiva disponible en `http://localhost:8080/swagger-ui/index.html` una vez levantada la app.

### Autenticación

```http
POST /api/auth/register
POST /api/auth/login
```

El login devuelve un JWT Bearer token que va en el header de cada request protegido:

```
Authorization: Bearer <token>
```

### Endpoints principales

```
# Usuarios
GET    /api/users
GET    /api/users/{id}
POST   /api/users
PUT    /api/users/{id}
DELETE /api/users/{id}

# Productos
GET    /api/products          # paginado, 300+ registros
GET    /api/products/{id}
POST   /api/products
PUT    /api/products/{id}

# Facturas
GET    /api/invoices               # paginado
GET    /api/invoices/{id}          # incluye ítems y usuario
POST   /api/invoices
PUT    /api/invoices/{id}
DELETE /api/invoices/{id}
GET    /api/invoices/my-invoices   # facturas del usuario autenticado
```

---

## Tests

```bash
mvn test                          # correr todos
mvn test jacoco:report            # con reporte de cobertura
mvn test -Dtest=InvoiceServiceTest # test específico
```

Estado: **61/61 pasando**

---

## CI/CD

GitHub Actions compila y corre los 61 tests en cada push a main. Si algo falla, el merge no pasa. Sin excepciones.

---

## Seguridad

JWT stateless, BCrypt para contraseñas, autorización por roles con Spring Security, validación de inputs en todos los endpoints,
queries parametrizadas mediante JPA (protección contra SQL injection).

---

## Contacto

**Samir Martinez Acosta** —(samiresteyber0511@hotmail.com)  
[GitHub](https://github.com/samirmartinez1984)
