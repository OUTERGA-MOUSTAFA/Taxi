package com.taxi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PointArretRequest(
    @NotBlank (message = "La ville est obligatoire")
    String ville,

    @NotNull (message = "L'heure de passage est obligatoire")
    LocalDateTime heurePassage,

    @NotNull(message = "Le prix depuis le départ est obligatoire")
    @DecimalMin (value = "0.00", message = "Le prix ne peut pas être négatif")
    BigDecimal prixDepuisDepart
) {}
