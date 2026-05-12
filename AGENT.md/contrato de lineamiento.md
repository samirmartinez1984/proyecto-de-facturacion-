# 🎯 ARCHITECTURE_AGENT.md - Sistema de Facturación
## 🧩 Contrato Maestro de Desarrollo (Monolito Multicapa)

Este documento es la **única fuente de verdad**. El Agente debe leer este archivo antes de generar cualquier código para asegurar la consistencia del proyecto.

---

### 🏛️ SKILL 1: CAPA DE MODELO (JPA & Persistence)

#### 1.1 Estándares de Implementación (Lombok & JPA)
- **Prohibición:** NO usar `@Data` en entidades.
- **Obligación:** Usar `@Getter`, `@Setter`, `@NoArgsConstructor` y `@AllArgsConstructor`.
- **Identidad:** Implementar `equals()` y `hashCode()` usando solo el `id` con `@EqualsAndHashCode(onlyExplicitlyIncluded = true)`.
- **ToString:** Usar `@ToString` pero excluyendo relaciones (`@ToString.Exclude`) para evitar recursividad infinita.
- **Auditoría:** Todas las tablas deben tener `created_at` (`LocalDateTime`).
- **Regla de auditoría:** `created_at` se asigna en backend (entidad/servicio), nunca desde DTO de entrada del cliente.

#### 1.2 Diccionario de Datos Obligatorio (Campos de Entidades)

Cualquier entidad generada debe seguir estos nombres y tipos de forma estricta:

**A. Entidad `User` (Tabla: `users`)**
- `id`: Long (PK, Identity)
- `username`: String (Unique, Not Null)
- `password`: String (Not Null, para BCrypt)
- `email`: String (Unique, Not Null)
- `enabled`: Boolean
- `roles`: Set<Role> (ManyToMany, FetchType.EAGER)

**B. Entidad `Role` (Tabla: `roles`)**
- `id`: Long (PK, Identity)
- `name`: String (Unique, Not Null) -> Ej: `ROLE_ADMIN`

**C. Entidad `Invoice` (Tabla: `invoices`)**
- `id`: Long (PK, Identity)
- `folio`: String (Unique, Not Null)
- `description`: String
- `status`: Enum String (Not Null) -> `DRAFT`, `PAID`, `CANCELLED`
- `subtotal`: BigDecimal (Precision 10, Scale 2)
- `tax_amount`: BigDecimal (Precision 10, Scale 2)
- `total`: BigDecimal (Precision 10, Scale 2)
- `created_at`: LocalDateTime
- `items`: List<InvoiceItem> (OneToMany, mappedBy = `"invoice"`, CascadeType.ALL, orphanRemoval = true)

**D. Entidad `InvoiceItem` (Tabla: `invoice_items`)**
- `id`: Long (PK, Identity)
- `product_name`: String (Not Null)
- `quantity`: Integer (Not Null)
- `unit_price`: BigDecimal (Precision 10, Scale 2)
- `line_total`: BigDecimal (Precision 10, Scale 2) -> (`quantity * unit_price`)
- `invoice`: Invoice (ManyToOne, JoinColumn `"invoice_id"`)

---

### 🛠️ SKILL 2: PERSISTENCIA Y MIGRACIONES
- **Flyway:** Todo cambio se realiza vía scripts `.sql` en `src/main/resources/db/migration`.
- **Nomenclatura SQL:** Tablas en plural y minúsculas. Columnas en `snake_case`.
- **Estrategia:** `spring.jpa.hibernate.ddl-auto=validate`.
- **No atajos:** No crear/modificar tablas manualmente fuera de Flyway.
- **Consistencia:** Tipos, nombres de columnas y restricciones deben coincidir entre entidad y migración.

---

### 🔐 SKILL 3: SEGURIDAD (JWT)
- **Algoritmo:** HS512.
- **Ubicación:** Header `Authorization: Bearer <token>`.
- **Roles:** El sistema debe verificar `ROLE_ADMIN` para creación/borrado y `ROLE_USER` para consultas propias.
- **Aplicación por capa:** La autorización se valida en Controller/Service; Repository y Mapper no deben conocer reglas de seguridad.

---

### 🗄️ SKILL 4: CAPA REPOSITORY

#### 4.1 Reglas Generales
- Cada entidad principal debe tener su interfaz en `com.sistema.facturacion2.repository`.
- Los repositorios deben extender `JpaRepository<Entidad, Long>`.
- Se permiten métodos derivados de Spring Data (`findBy...`, `existsBy...`) como primera opción.
- Usar `@Query` solo si la consulta no puede expresarse de forma clara con método derivado.
- El repository no contiene lógica de negocio ni reglas de validación.

#### 4.2 Buenas Prácticas
- Usar `Optional<T>` para búsquedas por llave o campos únicos.
- Usar `List<T>` o `Page<T>` para colecciones, según necesidad del endpoint.
- Evitar duplicidad de repositorios (mantener una sola versión por entidad).
- Mantener nombres de métodos descriptivos y alineados a casos de uso reales.

---

### 🔁 SKILL 5: CAPA MAPPER

#### 5.1 Responsabilidad Única
- El mapper solo convierte **Entidad <-> DTO**.
- **Prohibido:** lógica de negocio, acceso a base de datos o decisiones de autorización.
- Debe mantenerse limpio, predecible y reutilizable.

#### 5.2 Convenciones
- Métodos estándar sugeridos: `toDto`, `toEntity`, `updateEntityFromDto`.
- Todo mapper debe manejar `null` de forma segura.
- Conversiones de listas deben centralizarse en métodos privados reutilizables.
- Si un DTO de respuesta requiere resumen de relación (ejemplo: usuario en factura), usar DTO resumido y no exponer entidades.

#### 5.3 Regla de Auditoría en Mappers
- En mapeo de entrada (`Create*DTO`, `Update*DTO`) **no** mapear `createdAt` desde cliente.
- En mapeo de salida (`*DTO`) sí se expone `createdAt` proveniente de la entidad.

---

###  SKILL 6: EXCEPCIONES Y MANEJO GLOBAL

#### 6.1 Estructura Obligatoria
- Paquete: `com.sistema.facturacion2.exception`.
- Excepciones personalizadas mínimas:
  - `RecursoNoEncontradoException`
  - `DatosInvalidosException`
  - `ConflictoException`
  - `NoAutorizadoException`
- Todas deben extender `RuntimeException`.

#### 6.2 Contrato de Error de API
- Centralizar respuestas en `GlobalExceptionHandler` con `@RestControllerAdvice`.
- Usar un contrato uniforme (`ApiError`) para todos los errores.
- `ApiError` debe incluir como mínimo: `status`, `error`, `message`, `path`, `details`.
- `timestamp` es recomendado; si se usa en `ApiError`, debe enviarse siempre desde el handler.

#### 6.3 Mapeo HTTP Estándar
- `RecursoNoEncontradoException` -> `404 NOT_FOUND`
- `DatosInvalidosException` -> `400 BAD_REQUEST`
- `ConflictoException` -> `409 CONFLICT`
- `NoAutorizadoException` -> `401 UNAUTHORIZED`
- `MethodArgumentNotValidException` -> `400 BAD_REQUEST` con detalle por campo
- `Exception` genérica -> `500 INTERNAL_SERVER_ERROR`

---

### 🧠 SKILL 7: CAPA SERVICE

#### 7.1 Responsabilidades
- Implementar la lógica de negocio y casos de uso.
- Orquestar repositorios, mappers y validaciones de dominio.
- Lanzar excepciones personalizadas cuando corresponda.
- Devolver DTOs de salida, no entidades JPA al controlador.

#### 7.2 Reglas Técnicas
- Anotar con `@Service`.
- Usar `@Transactional(readOnly = true)` en consultas.
- Usar `@Transactional` en operaciones de escritura (create/update/delete).
- Validar reglas de negocio aquí (existencia, unicidad, stock, estado de factura, etc.).
- Asignar campos de sistema en backend (`createdAt`, folio, cálculos monetarios, etc.).

#### 7.3 Flujo Esperado
- Recibe DTO de entrada desde Controller.
- Consulta/valida con Repository.
- Convierte con Mapper.
- Aplica reglas de negocio.
- Persiste con Repository.
- Retorna DTO de respuesta.

---

### 🌐 SKILL 8: CAPA CONTROLLER (API REST)

#### 8.1 Responsabilidad
- Exponer endpoints HTTP y delegar al Service.
- Mantener controladores delgados (sin lógica de negocio compleja).

#### 8.2 Reglas de Implementación
- Anotar con `@RestController` y `@RequestMapping` por recurso.
- Usar DTOs (`Create*DTO`, `Update*DTO`, `*DTO`) en entrada/salida.
- Aplicar `@Valid` en request body para validaciones declarativas.
- Responder con `ResponseEntity` y estado HTTP correcto.
- Nunca acceder directamente a `Repository` desde Controller.

#### 8.3 Convención de Endpoints
- `POST /api/{resource}` -> crear
- `GET /api/{resource}` -> listar
- `GET /api/{resource}/{id}` -> obtener por id
- `PUT /api/{resource}/{id}` -> actualizar
- `DELETE /api/{resource}/{id}` -> eliminar

---

### ✅ SKILL 9: FLUJO DE CAPAS OBLIGATORIO
- Flujo estándar: `Controller -> Service -> Repository`.
- Los `Mapper` participan en frontera de datos: entrada/salida de Service.
- Las excepciones nacen típicamente en Service y se resuelven en `GlobalExceptionHandler`.
- Prohibido saltar capas (ejemplo: Controller invocando Repository directo).
- Mantener separación estricta de responsabilidades para facilitar mantenimiento y pruebas.

