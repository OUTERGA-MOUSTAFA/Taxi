package com.taxi.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TrajetRequest(
    @NotNull (message = "L'identifiant du conducteur est obligatoire")
    Long conducteurId,

    @NotNull(message = "L'identifiant du véhicule est obligatoire")
    Long vehiculeId,

    @Min (value = 1, message = "Le nombre de places proposées doit être au moins de 1")
    int placesProposees,

    @NotNull(message = "La liste des points d'arrêt est obligatoire")
    @Size (min = 2, message = "Un trajet doit contenir au moins 2 points d'arrêt")
    @Valid 
    List<PointArretRequest> pointsArret
) {}