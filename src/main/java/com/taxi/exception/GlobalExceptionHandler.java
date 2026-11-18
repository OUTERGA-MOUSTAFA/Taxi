package com.taxi.exception;

import java.net.URI;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicateResourceException(DuplicateResourceException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Conflit de données");
        problemDetail.setType(URI.create("https://api.taxi.com/errors/conflict"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail; // غيرجع HTTP 409 تلقائياً
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Ressource non trouvée");
        problemDetail.setType(URI.create("https://api.taxi.com/errors/not-found"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail; // غيرجع HTTP 404 تلقائياً
    }
}
