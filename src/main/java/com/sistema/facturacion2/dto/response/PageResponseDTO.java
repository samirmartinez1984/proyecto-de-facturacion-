package com.sistema.facturacion2.dto.response;

import java.util.List;

/**
 * DTO genérico de respuesta paginada.
 *
 * <p>Envuelve el resultado de cualquier consulta paginada del sistema,
 * exponiendo tanto el contenido de la página actual como la metadata
 * de navegación necesaria para que el frontend gestione la paginación.</p>
 *
 * @param <T>           tipo del elemento contenido en la página
 * @param content       lista de elementos de la página actual
 * @param pageNumber    número de la página actual (base 0)
 * @param pageSize      cantidad máxima de elementos por página
 * @param totalElements total de registros existentes en la base de datos
 * @param totalPages    total de páginas disponibles
 * @param isLast        {@code true} si esta es la última página
 */
public record PageResponseDTO<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isLast) {}
