package com.taxi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taxi.dto.VehiculeRequest;
import com.taxi.dto.VehiculeResponse;
import com.taxi.service.VehiculeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping ("/api/vehicules")
@RequiredArgsConstructor 
public class VehiculeController {
    
    private final VehiculeService vehiculeService;

    @PostMapping
    public ResponseEntity<VehiculeResponse> create(@RequestBody @Valid VehiculeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculeService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculeService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculeResponse> update(@PathVariable Long id, @RequestBody @Valid VehiculeRequest request) {
        return ResponseEntity.ok(vehiculeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehiculeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
