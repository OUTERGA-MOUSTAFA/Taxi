package com.taxi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public  record TrajetRechercheResponse(
    Long trajetId,
    String villeMontee,
    LocalDateTime heureMontee,
    String villeDescente,
    LocalDateTime heureDescente,
    BigDecimal prixSegment,
    Long placesRestantes,
    String conducteurNom
) {}