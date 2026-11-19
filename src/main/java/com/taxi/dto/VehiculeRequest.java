package com.taxi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehiculeRequest(
    @NotBlank (message = "La marque est obligatoire")
    String marque,

    @NotBlank(message = "Le modèle est obligatoire")
    String modele,

    @NotBlank(message = "L'immatriculation est obligatoire")
    String immatriculation,

    String couleur,

    @Min (value = 1, message = "Le minimum doit être au moins de 1 place")
    @Max (value = 8, message = "Le maximum ne peut pas dépasser 8 places")
    int capacite,

    @NotNull (message = "L'identifiant du conducteur est obligatoire")
    Long conducteurId
) {}