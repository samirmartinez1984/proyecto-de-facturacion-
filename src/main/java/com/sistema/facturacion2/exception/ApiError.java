package com.sistema.facturacion2.exception;

import java.util.List;



public record ApiError(
        int status,
        String error,
        String message,
        String path,
        List<String>details
){}
