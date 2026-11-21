package com.taxi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
    @NotNull (message = "L'identifiant du passager est obligatoire")
    Long passagerId,

    @NotNull(message = "L'identifiant du trajet est obligatoire")
    Long trajetId,

    @NotNull(message = "Le point de montée est obligatoire")
    Long pointMonteeId,

    @NotNull(message = "Le point de descente est obligatoire")
    Long pointDescenteId,

    @Min (value = 1, message = "Le nombre de places doit être au moins de 1")
    int nombrePlaces
) {}
