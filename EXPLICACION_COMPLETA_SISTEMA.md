# 🎯 EXPLICACIÓN COMPLETA DEL SISTEMA DE FACTURACIÓN

## 📊 DIAGRAMA DE RELACIONES DE BASE DE DATOS

```
┌─────────────────────┐    ┌─────────────────────┐    ┌─────────────────────┐
│      USERS          │    │    USER_ROLES       │    │       ROLES         │
├─────────────────────┤    ├─────────────────────┤    ├─────────────────────┤
│ id (PK)            │◄───┤ user_id (FK)        │───►│ id (PK)            │
│ username (UNIQUE)   │    │ role_id (FK)        │    │ name (UNIQUE)       │
│ password            │    └─────────────────────┘    │ created_at          │
│ email (UNIQUE)      │                               └─────────────────────┘
│ enabled             │
│ created_at          │
└─────────────────────┘
        │ 1
        │
        │ N
┌─────────────────────┐
│     INVOICES        │
├─────────────────────┤
│ id (PK)            │
│ folio (UNIQUE)      │
│ description         │
│ status (ENUM)       │
│ subtotal            │
│ tax_amount          │
│ total               │
│ user_id (FK)        │
│ created_at          │
└─────────────────────┘
        │ 1
        │
        │ N
┌─────────────────────┐
│   INVOICE_ITEMS     │
├─────────────────────┤
│ id (PK)            │
│ product_name        │
│ quantity            │
│ unit_price          │
│ line_total          │
│ invoice_id (FK)     │
└─────────────────────┘

┌─────────────────────┐
│     PRODUCTS        │    (Esta tabla NO tiene relación directa
├─────────────────────┤     con invoice_items, solo es un catálogo)
│ id (PK)            │
│ name                │
│ description         │
│ price               │
│ stock               │
│ enabled             │
│ created_at          │
└─────────────────────┘
```

## 🔍 EXPLICACIÓN DE CADA ENTIDAD Y SUS RELACIONES

### 1. 👤 **USUARIO (User.java)**

```java
@Entity
@Table(name = "users")
public class User {
    private Long id;                    // Clave primaria
    private String username;            // Nombre único de usuario
    private String password;            // Contraseña encriptada (BCrypt)
    private String email;               // Email único
    private Boolean enabled;            // Usuario activo/inactivo

    // RELACIONES:
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles")     // Tabla intermedia
    private Set<Role> roles;            // Un usuario puede tener muchos roles

    @OneToMany(mappedBy = "user")
    private List<Invoice> invoices;     // Un usuario puede crear muchas facturas
}
```

**¿Por qué Set<Role> y no un Enum?**

**ANTES (con Enum):**
```java
public enum UserRole {
    ADMIN, USER, MANAGER
}

// En la entidad User:
@Enumerated(EnumType.STRING)
private UserRole role;  // Solo UN rol por usuario
```

**AHORA (con Clases):**
```java
// Un usuario puede tener MÚLTIPLES roles simultáneamente:
Set<Role> roles = {ROLE_ADMIN, ROLE_USER, ROLE_MANAGER}
```

### 2. 🏷️ **ROL (Role.java)**

```java
@Entity
@Table(name = "roles")
public class Role {
    private Long id;           // Clave primaria
    private String name;       // Nombre del rol: "ROLE_ADMIN", "ROLE_USER"
}
```

**¿Por qué una clase separada para Role?**

✅ **VENTAJAS:**
- **Flexibilidad:** Puedes agregar nuevos roles sin modificar código
- **Múltiples roles:** Un usuario puede ser ADMIN y USER a la vez
- **Persistencia:** Los roles se almacenan en BD, no hardcodeados
- **Escalabilidad:** Puedes agregar permisos específicos a cada rol más tarde

❌ **DESVENTAJAS del Enum:**
- Un usuario solo puede tener UN rol
- Para agregar roles nuevos hay que modificar código
- Menos flexible para sistemas complejos

### 3. 🔗 **TABLA INTERMEDIA (user_roles)**

```sql
CREATE TABLE user_roles (
    user_id BIGINT,    -- FK hacia users.id
    role_id BIGINT,    -- FK hacia roles.id
    PRIMARY KEY (user_id, role_id)  -- Clave compuesta
);
```

**¿Cómo funciona la relación Many-to-Many?**

```
USUARIO "admin" puede tener:
- ROLE_ADMIN (id=1)
- ROLE_USER (id=2)

Tabla user_roles:
user_id | role_id
--------|--------
   1    |   1     (admin tiene ROLE_ADMIN)
   1    |   2     (admin tiene ROLE_USER)
   2    |   2     (otro usuario solo USER)
```

**Código en User.java:**
```java
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(
    name = "user_roles",               // Nombre de tabla intermedia
    joinColumns = @JoinColumn(name = "user_id"),        // FK hacia User
    inverseJoinColumns = @JoinColumn(name = "role_id")  // FK hacia Role
)
private Set<Role> roles = new HashSet<>();
```

### 4. 📄 **FACTURA (Invoice.java)**

```java
@Entity
@Table(name = "invoices")
public class Invoice {
    private Long id;
    private String folio;              // Número único de factura
    private InvoiceStatus status;      // AQUÍ SÍ usamos ENUM (DRAFT/PAID/CANCELLED)
    private BigDecimal total;

    // RELACIONES:
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;                 // Muchas facturas pertenecen a UN usuario

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    private List<InvoiceItem> items;   // Una factura tiene muchos items
}
```

**¿Por qué InvoiceStatus SÍ es un Enum?**
- Los estados de factura son **fijos** y **limitados**: DRAFT, PAID, CANCELLED
- No necesitan flexibilidad como los roles de usuario
- Es más eficiente almacenar como String en BD

### 5. 📦 **ITEM DE FACTURA (InvoiceItem.java)**

```java
@Entity
@Table(name = "invoice_items")
public class InvoiceItem {
    private Long id;
    private String productName;        // Copia del nombre del producto
    private Integer quantity;
    private BigDecimal unitPrice;      // Precio en el momento de la venta
    private BigDecimal lineTotal;      // quantity * unitPrice (calculado automáticamente)

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;           // Muchos items pertenecen a UNA factura
}
```

**¿Por qué NO hay relación directa con Product?**
- Cuando se crea una factura, se **copia** la información del producto
- Si después cambias el precio del producto, las facturas anteriores mantienen el precio original
- Es un **snapshot** del producto en el momento de la venta

### 6. 🛍️ **PRODUCTO (Product.java)**

```java
@Entity
@Table(name = "products")
public class Product {
    private Long id;
    private String name;
    private BigDecimal price;      // Precio actual
    private Integer stock;         // Inventario actual
    private Boolean enabled;       // Producto activo/inactivo
}
```

**Product es independiente** - Es solo un catálogo para crear facturas.

## 🔄 FLUJO DE DATOS COMPLETO

### Paso 1: Autenticación
```java
// 1. Usuario "admin" se autentica
User admin = userRepository.findByUsername("admin");

// 2. Spring Security carga sus roles
Set<Role> adminRoles = admin.getRoles();
// adminRoles contiene: [ROLE_ADMIN, ROLE_USER]

// 3. Se crean las authorities de Spring Security
adminRoles.stream()
    .map(role -> new SimpleGrantedAuthority(role.getName()))
    .collect(Collectors.toSet());
```

### Paso 2: Creación de Factura
```java
// 1. Crear nueva factura
Invoice invoice = new Invoice();
invoice.setUser(currentUser);
invoice.setFolio("FAC-001");
invoice.setStatus(InvoiceStatus.DRAFT);

// 2. Agregar items basados en productos del catálogo
Product product = productRepository.findById(1L);

InvoiceItem item = new InvoiceItem();
item.setProductName(product.getName());    // COPIA el nombre
item.setUnitPrice(product.getPrice());     // COPIA el precio actual
item.setQuantity(2);

// 3. Vincular item con factura (relación bidireccional)
invoice.addItem(item);  // Método helper que sincroniza ambos lados

// 4. Guardar (Cascade.ALL guarda automáticamente los items)
invoiceRepository.save(invoice);
```

### Paso 3: Consultas Complejas

#### ⚡ CON ENTITY GRAPHS (Recomendado - Optimizado):
```java
// Buscar todas las facturas con paginación y usuario cargado
@EntityGraph(attributePaths = {"user"})
Page<Invoice> findAll(Pageable pageable);

// Buscar factura con usuario e items sin problema N+1
@EntityGraph(attributePaths = {"user", "items"})
Optional<Invoice> findById(Long id);

// Buscar facturas de usuario por username sin problema N+1
@EntityGraph(attributePaths = {"user"})
@Query("SELECT i FROM Invoice i JOIN i.user u WHERE u.username = :username")
List<Invoice> findByUsernameWithUser(@Param("username") String username);
```

**Beneficio:** Evita el problema N+1 automáticamente cuando mapeas a DTOs.

#### Consultas adicionales sin optimización:
```java
// Buscar usuarios que tengan un rol específico
@Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
List<User> findUsersByRoleName(@Param("roleName") String roleName);
```

📌 **Ver [OPTIMIZACION_N+1.md](./OPTIMIZACION_N+1.md) para análisis detallado de cómo funcionan Entity Graphs.**

## ⚙️ CONFIGURACIÓN EN application.properties

```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/facturacion_db
spring.datasource.username=root
spring.datasource.password=password

# Flyway para migraciones
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate  # Solo valida, NO modifica la BD
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Security
spring.security.user.name=admin
spring.security.user.password=admin123
```

## 🔐 CONFIGURACIÓN DE SPRING SECURITY

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Para encriptar contraseñas
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            User user = userRepository.findByUsername(username);

            // Convertir roles a authorities de Spring Security
            Collection<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());

            return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getEnabled(),
                true, true, true,
                authorities
            );
        };
    }
}
```

## 📋 RESUMEN DE DIFERENCIAS: ENUM vs CLASE

| Aspecto | Role como ENUM | Role como CLASE |
|---------|----------------|-----------------|
| **Flexibilidad** | Baja - hardcoded | Alta - dinámico |
| **Múltiples roles** | ❌ No | ✅ Sí |
| **Agregar roles** | Modificar código | Solo BD |
| **Complejidad** | Baja | Media |
| **Rendimiento** | Mejor | Bueno |
| **Casos de uso** | Sistemas simples | Sistemas escalables |

## 🎯 CONCLUSIÓN

**¿Cuándo usar cada enfoque?**

**ENUM (como InvoiceStatus):**
- Estados fijos y limitados
- No requiere flexibilidad
- Mejor rendimiento

**CLASE (como Role):**
- Necesitas flexibilidad
- Múltiples asignaciones
- Sistema escalable
- Administración dinámica

En tu sistema actual, los **roles son clases** porque es un sistema profesional que necesita flexibilidad para crecer y manejar usuarios con múltiples permisos simultáneamente.
