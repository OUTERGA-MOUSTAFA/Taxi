package com.taxi.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taxi.dto.ReservationResponse;
import com.taxi.dto.TrajetRechercheResponse;
import com.taxi.dto.TrajetRequest;
import com.taxi.dto.TrajetResponse;
import com.taxi.service.TrajetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trajets")
@RequiredArgsConstructor// sawb constructure automatique dyale les field li final o @NotNull
public class TrajetController {

    private final TrajetService trajetService;

    @PostMapping
    public ResponseEntity<TrajetResponse> publierTrajet(@RequestBody @Valid TrajetRequest request) {
        TrajetResponse response = trajetService.publierTrajet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);// 201 Created
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrajetResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(trajetService.findById(id));// 200 OK
    }

    @PatchMapping("/{id}/terminer")
    public ResponseEntity<Void> terminerTrajet(@PathVariable Long id, @RequestParam Long conducteurId) {
        trajetService.terminerTrajet(id, conducteurId);
        return ResponseEntity.noContent().build();// 204 No Content
    }

    @GetMapping("/recherche")
    public ResponseEntity<Page<TrajetRechercheResponse>> rechercher(
            @RequestParam String villeDepart,
            @RequestParam String villeArrivee,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") int places,
            @RequestParam(required = false) BigDecimal prixMax,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        Page<TrajetRechercheResponse> page = trajetService.rechercherTrajets(
                villeDepart, villeArrivee, date, places, prixMax, pageable);
        return ResponseEntity.ok(page);// 200 ok
    }

    // conducteur kay annulee trajet dyale
    @PatchMapping("/{id}/annuler")
    public ResponseEntity<Void> annulerTrajet(
            @PathVariable Long id,
            @RequestParam Long conducteurId) {
        trajetService.annulerTrajet(id, conducteurId);
        return ResponseEntity.noContent().build();
    }

    // consulé les reservations dyale trajet x
    @GetMapping("/{id}/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservationsByTrajet(
            @PathVariable Long id,
            @RequestParam Long conducteurId) {
        return ResponseEntity.ok(trajetService.findReservationsByTrajetId(id, conducteurId));
    }
}