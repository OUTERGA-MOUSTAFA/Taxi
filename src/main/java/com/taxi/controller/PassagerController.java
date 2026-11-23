package com.taxi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taxi.dto.PassagerRequest;
import com.taxi.dto.PassagerResponse;
import com.taxi.dto.ReservationResponse;
import com.taxi.service.PassagerService;
import com.taxi.service.ReservationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/passagers")
@RequiredArgsConstructor // sawb constructure automatique dyale les field li final o @NotNull
public class PassagerController {
    private final PassagerService passagerService;
    private final ReservationService reservationService;
    // public PassagerController(PassagerService passagerService){
    // this.passagerService = passagerService;
    // }

    @PostMapping
    public ResponseEntity<PassagerResponse> create(@RequestBody @Valid PassagerRequest request) {
        return new ResponseEntity<>(passagerService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PassagerResponse>> getAll() {
        return ResponseEntity.ok(passagerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassagerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(passagerService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PassagerResponse> update(@PathVariable Long id, @RequestBody @Valid PassagerRequest request) {
        return ResponseEntity.ok(passagerService.update(id, request));
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<PassagerResponse>> search(@RequestParam(required = false) String email) {
        return ResponseEntity.ok(passagerService.searchByEmail(email));
    }

    // jib les reservations dyali
    @GetMapping("/{id}/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservationsByPassager(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.findByPassagerId(id));
    }
}
