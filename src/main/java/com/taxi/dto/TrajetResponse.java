package com.taxi.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.taxi.enums.StatutTrajet;

public record TrajetResponse(
    Long id,
    Long conducteurId,
    String conducteurNom,
    Long vehiculeId,
    String vehiculeImmatriculation,
    String villeDepart,
    String villeArrivee,
    LocalDateTime dateDepart,
    int placesProposees,
    int placesRestantes, // kaythseb f Service o makaynsh f entity
    StatutTrajet statut,
    List<PointArretResponse> pointsArret
) {}
