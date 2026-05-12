# 📦 DTOs — Sistema de Facturación

---

## 📁 Estructura de paquetes

```
dto/
├── request/
│   ├── LoginRequestDTO.java
│   ├── CreateUserDTO.java
│   ├── UpdateUserDTO.java
│   ├── CreateRoleDTO.java
│   ├── CreateProductDTO.java
│   ├── UpdateProductDTO.java
│   ├── CreateInvoiceDTO.java
│   ├── CreateInvoiceItemDTO.java
│   └── UpdateInvoiceDTO.java
│
└── response/
    ├── AuthResponseDTO.java
    ├── UserDTO.java
    ├── RoleDTO.java
    ├── ProductDTO.java
    ├── InvoiceDTO.java
    ├── InvoiceDetailDTO.java
    └── InvoiceItemDTO.java
```

---

# ➡️ REQUEST DTOs

### `LoginRequestDTO`
| Campo      | Tipo     |
|------------|----------|
| `username` | `String` |
| `password` | `String` |

---

### `CreateUserDTO`
| Campo      | Tipo     |
|------------|----------|
| `username` | `String` |
| `password` | `String` |
| `email`    | `String` |

---

### `UpdateUserDTO`
| Campo     | Tipo      |
|-----------|-----------|
| `email`   | `String`  |
| `enabled` | `Boolean` |

---

### `CreateRoleDTO`
| Campo  | Tipo     |
|--------|----------|
| `name` | `String` |

---

### `CreateProductDTO`
| Campo         | Tipo         |
|---------------|--------------|
| `name`        | `String`     |
| `description` | `String`     |
| `price`       | `BigDecimal` |
| `stock`       | `Integer`    |

---

### `UpdateProductDTO`
| Campo         | Tipo         |
|---------------|--------------|
| `name`        | `String`     |
| `description` | `String`     |
| `price`       | `BigDecimal` |
| `stock`       | `Integer`    |
| `enabled`     | `Boolean`    |

---

### `CreateInvoiceDTO`
| Campo         | Tipo                         |
|---------------|------------------------------|
| `description` | `String`                     |
| `items`       | `List<CreateInvoiceItemDTO>` |

---

### `CreateInvoiceItemDTO`
| Campo         | Tipo         |
|---------------|--------------|
| `productName` | `String`     |
| `quantity`    | `Integer`    |
| `unitPrice`   | `BigDecimal` |

---

### `UpdateInvoiceDTO`
| Campo         | Tipo                         |
|---------------|------------------------------|
| `description` | `String`                     |
| `status`      | `InvoiceStatus`              |
| `items`       | `List<CreateInvoiceItemDTO>` |

---

# ⬅️ RESPONSE DTOs

### `AuthResponseDTO`
| Campo   | Tipo      |
|---------|-----------|
| `token` | `String`  |
| `type`  | `String`  |
| `user`  | `UserDTO` |

---

### `UserDTO`
| Campo       | Tipo            |
|-------------|-----------------|
| `id`        | `Long`          |
| `username`  | `String`        |
| `email`     | `String`        |
| `enabled`   | `Boolean`       |
| `roles`     | `Set<RoleDTO>`  |
| `createdAt` | `LocalDateTime` |

---

### `RoleDTO`
| Campo       | Tipo            |
|-------------|-----------------|
| `id`        | `Long`          |
| `name`      | `String`        |
| `createdAt` | `LocalDateTime` |

---

### `ProductDTO`
| Campo         | Tipo            |
|---------------|-----------------|
| `id`          | `Long`          |
| `name`        | `String`        |
| `description` | `String`        |
| `price`       | `BigDecimal`    |
| `stock`       | `Integer`       |
| `enabled`     | `Boolean`       |
| `createdAt`   | `LocalDateTime` |

---

### `InvoiceDTO`
| Campo         | Tipo            |
|---------------|-----------------|
| `id`          | `Long`          |
| `folio`       | `String`        |
| `description` | `String`        |
| `status`      | `InvoiceStatus` |
| `subtotal`    | `BigDecimal`    |
| `taxAmount`   | `BigDecimal`    |
| `total`       | `BigDecimal`    |
| `createdAt`   | `LocalDateTime` |
| `userId`      | `Long`          |

---

### `InvoiceDetailDTO`
| Campo         | Tipo                   |
|---------------|------------------------|
| `id`          | `Long`                 |
| `folio`       | `String`               |
| `description` | `String`               |
| `status`      | `InvoiceStatus`        |
| `subtotal`    | `BigDecimal`           |
| `taxAmount`   | `BigDecimal`           |
| `total`       | `BigDecimal`           |
| `createdAt`   | `LocalDateTime`        |
| `user`        | `UserDTO`              |
| `items`       | `List<InvoiceItemDTO>` |

---

### `InvoiceItemDTO`
| Campo         | Tipo         |
|---------------|--------------|
| `id`          | `Long`       |
| `productName` | `String`     |
| `quantity`    | `Integer`    |
| `unitPrice`   | `BigDecimal` |
| `lineTotal`   | `BigDecimal` |


