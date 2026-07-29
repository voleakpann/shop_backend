package com.ministore.blog.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/** Turns common failures into clean 400/404/409 responses instead of raw 500s. */
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

    /** Unknown slug or id → 404. */
    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<Map<String, Object>> onPostNotFound(PostNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }

    /** Slug already taken → 409, which is more useful to the client than a DB constraint 500. */
    @ExceptionHandler(DuplicateSlugException.class)
    public ResponseEntity<Map<String, Object>> onDuplicateSlug(DuplicateSlugException ex) {
        return ResponseEntity.status(409).body(Map.of("error", ex.getMessage()));
    }
}
