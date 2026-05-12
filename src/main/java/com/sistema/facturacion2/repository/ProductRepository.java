package com.sistema.facturacion2.repository;

import com.sistema.facturacion2.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio simple para gestión de productos
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Buscar producto por nombre exacto
     */
    Optional<Product> findByName(String name);
    
    /**
     * Validar si producto ya existe
     */
    boolean existsByName(String name);
    
    /**
     * Filtrar productos activos/inactivos
     */
    List<Product> findByEnabled(Boolean enabled);
    
    /**
     * Buscar productos que contengan texto en el nombre
     */
    @Query("SELECT p FROM Product p WHERE UPPER(p.name) LIKE UPPER(CONCAT('%', :namePattern, '%'))")
    List<Product> findByNameContaining(@Param("namePattern") String namePattern);
    
    /**
     * Productos con stock bajo (alerta de inventario)
     */
    @Query("SELECT p FROM Product p WHERE p.stock <= :minStock AND p.enabled = true")
    List<Product> findLowStockProducts(@Param("minStock") Integer minStock);
    
    /**
     * Solo productos activos ordenados por nombre
     */
    @Query("SELECT p FROM Product p WHERE p.enabled = true ORDER BY p.name ASC")
    List<Product> findActiveProductsByName();
}

