package com.taxi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.taxi.enums.StatutReservation;

public record ReservationResponse(Long id,
        Long passagerId,
        String passagerNom,
        Long trajetId,
        Long pointMonteeId,
        String villeMontee,
        Long pointDescenteId,
        String villeDescente,
        int nombrePlaces,
        BigDecimal montantTotal,
        LocalDateTime dateReservation,
        StatutReservation statut) {

}
