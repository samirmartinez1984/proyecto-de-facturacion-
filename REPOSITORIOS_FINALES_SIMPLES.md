# ✅ REPOSITORIOS SIMPLIFICADOS - LIMPIOS Y LISTOS

## 📁 **ESTRUCTURA FINAL**

Los repositorios han sido **simplificados y limpiados**. Ahora tienes **5 repositorios simples** sin duplicados ni complejidad innecesaria:

```
src/main/java/com/sistema/facturacion2/repository/
├── UserRepository.java          👤 5 métodos esenciales para usuarios
├── RoleRepository.java          🏷️ 2 métodos básicos para roles
├── InvoiceRepository.java       📄 7 métodos principales para facturas
├── InvoiceItemRepository.java   📦 2 métodos para items de factura
└── ProductRepository.java       🛍️ 6 métodos para productos
```

## 🎯 **MÉTODOS ESENCIALES POR REPOSITORIO**

### 👤 **UserRepository** - Para autenticación
```java
Optional<User> findByUsername(String username);           // Login
Optional<User> findByEmail(String email);                 // Buscar por email
boolean existsByUsername(String username);                // Validar registro
boolean existsByEmail(String email);                      // Validar email único
Optional<User> findByUsernameWithRoles(String username);  // Para Spring Security
```

### 🏷️ **RoleRepository** - Para roles
```java
Optional<Role> findByName(String name);          // Buscar rol: "ROLE_ADMIN"
boolean existsByName(String name);              // Validar si rol existe
```

### 📄 **InvoiceRepository** - Para facturas

#### Métodos Básicos:
```java
Optional<Invoice> findByFolio(String folio);              // Buscar por número: "FAC-001"
boolean existsByFolio(String folio);                      // Validar folio único
List<Invoice> findByUserId(Long userId);                  // Mis facturas
List<Invoice> findByStatus(InvoiceStatus status);         // Por estado: PAID, DRAFT
Long countByStatus(InvoiceStatus status);                 // Contar por estado
```

#### ⚡ Métodos Optimizados (CON ENTITY GRAPHS - evitan N+1):
```java
Page<Invoice> findAll(Pageable pageable);                 // Con usuario cargado (optimizado)
Optional<Invoice> findById(Long id);                      // Con usuario e items (optimizado)
List<Invoice> findByUsernameWithUser(String username);    // Por username con usuario (optimizado)
```

**📌 IMPORTANTE:** Los últimos 3 métodos usan `@EntityGraph` para evitar el problema N+1.
Ver [OPTIMIZACION_N+1.md](./OPTIMIZACION_N+1.md) para más detalles.

### 📦 **InvoiceItemRepository** - Para items
```java
List<InvoiceItem> findByInvoiceId(Long invoiceId);        // Items de una factura
List<InvoiceItem> findByProductName(String productName);  // Ventas de un producto
```

### 🛍️ **ProductRepository** - Para productos
```java
Optional<Product> findByName(String name);                      // Buscar producto
boolean existsByName(String name);                              // Validar si existe
List<Product> findByEnabled(Boolean enabled);                   // Activos/inactivos
List<Product> findByNameContaining(String namePattern);         // Búsqueda
List<Product> findLowStockProducts(Integer minStock);           // Stock bajo
List<Product> findActiveProductsByName();                       // Catálogo ordenado
```

## 📚 **MÉTODOS GRATIS con JpaRepository**

**¡Estos métodos ya los tienes automáticamente!** No necesitas crearlos:

```java
// Operaciones básicas que YA FUNCIONAN:
userRepository.save(user);              // Crear/actualizar
userRepository.findById(1L);             // Buscar por ID
userRepository.findAll();                // Obtener todos
userRepository.deleteById(1L);           // Eliminar
userRepository.count();                  // Contar total
userRepository.existsById(1L);           // Verificar si existe
```

## 🚀 **EJEMPLOS PRÁCTICOS DE USO**

### **Ejemplo 1: Login de usuario**
```java
@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    public User login(String username) {
        return userRepository.findByUsernameWithRoles(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}
```

### **Ejemplo 2: Crear factura**
```java
@Service
public class InvoiceService {
    @Autowired
    private InvoiceRepository invoiceRepository;

    public Invoice createInvoice(Invoice invoice) {
        // Validar folio único
        if (invoiceRepository.existsByFolio(invoice.getFolio())) {
            throw new RuntimeException("El folio ya existe");
        }

        // Guardar
        return invoiceRepository.save(invoice);
    }

    // Ver factura con sus items (sin problema N+1 gracias a Entity Graph)
    public InvoiceDetailDTO getInvoiceDetail(Long id) {
        Invoice invoice = invoiceRepository.findById(id)  // ✅ Optimizado con @EntityGraph
            .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        return invoiceMapper.toDetailDto(invoice);
    }

    // Ver facturas de usuario autenticado (sin problema N+1)
    public List<InvoiceDTO> getMyInvoices(String username) {
        return invoiceRepository.findByUsernameWithUser(username)  // ✅ Optimizado con @EntityGraph
            .stream()
            .map(invoiceMapper::toDto)
            .toList();
    }
}
```

### **Ejemplo 3: Catálogo de productos**
```java
@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    // Listar productos activos
    public List<Product> getCatalog() {
        return productRepository.findByEnabled(true);
    }

    // Buscar productos
    public List<Product> search(String keyword) {
        return productRepository.findByNameContaining(keyword);
    }

    // Alertas de stock bajo
    public List<Product> getLowStock() {
        return productRepository.findLowStockProducts(5);
    }
}
```

## ✅ **VENTAJAS DE LA VERSIÓN SIMPLIFICADA**

1. **✨ Sin duplicados** - Ya no hay confusión entre versiones
2. **🎯 Solo lo esencial** - Métodos que realmente vas a usar
3. **📝 Fácil de entender** - Sin comentarios excesivos ni emojis
4. **⚡ Compila perfecto** - Sin errores de sintaxis
5. **🚀 Listo para usar** - Puedes crear servicios inmediatamente
6. **⚡ Optimizado para producción** - Incluye Entity Graphs para evitar problema N+1

## ⚡ **OPTIMIZACIÓN N+1**

Los métodos de InvoiceRepository ahora incluyen **Entity Graphs** que evitan el problema classic N+1 en consultas con relaciones:

- `findAll(Pageable)` → Carga automáticamente el usuario
- `findById(Long)` → Carga automáticamente usuario e items
- `findByUsernameWithUser(String)` → Carga automáticamente el usuario

**Beneficio:** En lugar de 101 consultas para 100 facturas, solo haces 1 consulta. ✅

Para más detalles, ver [OPTIMIZACION_N+1.md](./OPTIMIZACION_N+1.md)

## 🎯 **PRÓXIMO PASO RECOMENDADO**

**¡Crear la capa Service!** Con estos repositorios simples ya puedes empezar a crear servicios sin complicaciones.

```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User createUser(User user) {
        // Usar los métodos del repositorio
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Usuario ya existe");
        }
        return userRepository.save(user);
    }
}
```

¡Los repositorios están **perfectos y listos** para continuar con el desarrollo! 🎉
