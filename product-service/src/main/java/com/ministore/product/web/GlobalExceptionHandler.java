package com.ministore.product.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/** Turns common failures into clean 4xx responses instead of raw 500s. */
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

    /** e.g. a duplicate slug hitting the unique constraint → 409 instead of 500. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> onConflict(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "A product with the same unique field (e.g. slug) already exists"));
    }
}
