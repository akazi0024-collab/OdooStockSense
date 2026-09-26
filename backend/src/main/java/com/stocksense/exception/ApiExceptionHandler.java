package com.stocksense.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NotFoundException.class) ResponseEntity<?> notFound(NotFoundException ex) { return response(HttpStatus.NOT_FOUND, ex.getMessage()); }
    @ExceptionHandler({BadRequestException.class, BadCredentialsException.class}) ResponseEntity<?> badRequest(RuntimeException ex) { return response(HttpStatus.BAD_REQUEST, ex.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
        Map<String,String> fields=new LinkedHashMap<>();
        for (FieldError error: ex.getBindingResult().getFieldErrors()) fields.putIfAbsent(error.getField(), error.getDefaultMessage());
        return ResponseEntity.badRequest().body(Map.of("timestamp",Instant.now(),"status",400,"error","Validation failed","message","Request validation failed","fields",fields));
    }
    @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<?> conflict(DataIntegrityViolationException ex) { return response(HttpStatus.CONFLICT,"A record with a conflicting unique value exists or is still referenced"); }
    @ExceptionHandler(Exception.class) ResponseEntity<?> unexpected(Exception ex) { return response(HttpStatus.INTERNAL_SERVER_ERROR,"An unexpected server error occurred"); }
    private ResponseEntity<?> response(HttpStatus status,String message) { return ResponseEntity.status(status).body(Map.of("timestamp",Instant.now(),"status",status.value(),"error",status.getReasonPhrase(),"message",message)); }
}
