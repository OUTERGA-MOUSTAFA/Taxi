package com.taxi.service;

import java.util.List;

import com.taxi.dto.ReservationRequest;
import com.taxi.dto.ReservationResponse;

public interface ReservationService {
    // create reservations
    ReservationResponse reserver(ReservationRequest request);

    // Consulter {id}/reservations
    List<ReservationResponse> findByPassagerId(Long passagerId);
    // annuler {id}/reservations b patch
    void annuler(Long reservationId, Long passagerId);
}
