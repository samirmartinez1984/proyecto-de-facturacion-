package com.sistema.facturacion2.repository;

import com.sistema.facturacion2.model.Invoice;
import com.sistema.facturacion2.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio simple para gestión de facturas
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    /**
     * Buscar factura por folio único
     */
    Optional<Invoice> findByFolio(String folio);
    
    /**
     * Verificar si existe factura con folio específico
     */
    boolean existsByFolio(String folio);
    
    /**
     * Buscar facturas por usuario
     */
    List<Invoice> findByUserId(Long userId);
    
    /**
     * Buscar facturas por estado
     */
    List<Invoice> findByStatus(InvoiceStatus status);
    
    /**
     * Factura con sus items - evita problema N+1
     */
    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.items WHERE i.id = :id")
    Optional<Invoice> findByIdWithItems(@Param("id") Long id);
    
    /**
     * Buscar facturas por username del usuario
     */
    @Query("SELECT i FROM Invoice i JOIN i.user u WHERE u.username = :username")
    List<Invoice> findByUsername(@Param("username") String username);
    
    /**
     * Contar facturas por estado
     */
    Long countByStatus(InvoiceStatus status);

    // ✅ NUEVOS MÉTODOS CON ENTITY GRAPH PARA EVITAR N+1

    /**
     * Buscar todas las facturas con paginación, cargando el usuario para evitar N+1
     */
    @EntityGraph(attributePaths = {"user"})
    Page<Invoice> findAll(Pageable pageable);

    /**
     * Buscar factura por ID con items y usuario para evitar N+1
     */
    @EntityGraph(attributePaths = {"user", "items"})
    Optional<Invoice> findById(Long id);

    /**
     * Buscar facturas por username cargando el usuario para evitar N+1
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT i FROM Invoice i JOIN i.user u WHERE u.username = :username")
    List<Invoice> findByUsernameWithUser(@Param("username") String username);
}
