package com.taxi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taxi.dto.ReservationRequest;
import com.taxi.dto.ReservationResponse;
import com.taxi.service.ReservationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> reserver(@RequestBody @Valid ReservationRequest request) {
        ReservationResponse response = reservationService.reserver(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // annuler reservations dyalek
    @PatchMapping("/{id}/annuler")
    public ResponseEntity<Void> annuler(@PathVariable Long id, @RequestParam Long passagerId) {
        reservationService.annulerReservation(id, passagerId);
        return ResponseEntity.noContent().build();
    }
}