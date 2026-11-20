package com.taxi.exception;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ProblemDetail probleme(HttpStatus status, String titre, String type, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(titre);
        problemDetail.setType(URI.create("https://api.taxi.com/errors/" + type));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    // 400 : champs invalides (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erreurs = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (premier, second) -> premier));
        ProblemDetail problemDetail = probleme(HttpStatus.BAD_REQUEST, "Erreur de validation", "validation",
                "Les données envoyées sont invalides.");
        problemDetail.setProperty("erreurs", erreurs);
        return problemDetail;
    }

    // 400 : JSON absent ou mal formé
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleJsonIllisible(HttpMessageNotReadableException ex) {
        return probleme(HttpStatus.BAD_REQUEST, "Requête illisible", "requete-illisible",
                "Le corps de la requête est absent ou n'est pas un JSON valide.");
    }

    // 400 : paramètre de mauvais type, par exemple /api/conducteurs/abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMauvaisType(MethodArgumentTypeMismatchException ex) {
        return probleme(HttpStatus.BAD_REQUEST, "Paramètre invalide", "parametre-invalide",
                "Le paramètre '" + ex.getName() + "' a une valeur invalide : " + ex.getValue());
    }

    // 403 : ressource d'un autre conducteur ou passager
    @ExceptionHandler(ForbiddenActionException.class)
    public ProblemDetail handleForbidden(ForbiddenActionException ex) {
        log.warn("Action interdite : {}", ex.getMessage());
        return probleme(HttpStatus.FORBIDDEN, "Action interdite", "forbidden", ex.getMessage());
    }

    // 404 : ressource introuvable
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        return probleme(HttpStatus.NOT_FOUND, "Ressource non trouvée", "not-found", ex.getMessage());
    }

    // 409 : doublon (email, permis, immatriculation)
    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicateResourceException(DuplicateResourceException ex) {
        return probleme(HttpStatus.CONFLICT, "Conflit de données", "conflict", ex.getMessage());
    }

    // 409 : règle métier violée (trajet complet, délai dépassé, trajet terminé...)
    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex) {
        return probleme(HttpStatus.CONFLICT, "Règle métier non respectée", "business-rule", ex.getMessage());
    }

    // 500 : tout le reste
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAutre(Exception ex) {
        // Les erreurs de Spring (route inconnue 404, méthode non supportée 405...)
        // gardent leur code
        if (ex instanceof ErrorResponse errorResponse) {
            ProblemDetail problemDetail = errorResponse.getBody();
            problemDetail.setProperty("timestamp", Instant.now());
            return problemDetail;
        }
        log.error("Erreur interne inattendue", ex);
        return probleme(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne", "internal",
                "Une erreur interne est survenue. Réessayez plus tard.");
    }

    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequestException(BadRequestException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Requête invalide");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail; // 400
    }

    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbiddenException(ForbiddenException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problemDetail.setTitle("Accès refusé");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail; // 403
    }
}