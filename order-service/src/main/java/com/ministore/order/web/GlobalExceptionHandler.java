package com.ministore.order.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/** Turns common failures into clean 400 responses instead of raw 500s. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Bean-validation failures on a request body → 400 with per-field messages. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> onValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> fields.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Validation failed",
                "fields", fields));
    }

    /** Ordering a product that doesn't exist → 400 with the offending slug. */
    @ExceptionHandler(UnknownProductException.class)
    public ResponseEntity<Map<String, Object>> onUnknownProduct(UnknownProductException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
