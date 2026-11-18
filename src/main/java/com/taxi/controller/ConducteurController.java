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

import com.taxi.dto.ConducteurRequest;
import com.taxi.dto.ConducteurResponse;
import com.taxi.service.ConducteurService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/conducteurs")
public class ConducteurController {

    private final ConducteurService conducteurService;

    public ConducteurController(ConducteurService conducteurService) {
        this.conducteurService = conducteurService;
    }

    @PostMapping
    public ResponseEntity<ConducteurResponse> create(@RequestBody @Valid ConducteurRequest request) {
        return new ResponseEntity<>(conducteurService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ConducteurResponse>> getAll() {
        return ResponseEntity.ok(conducteurService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConducteurResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(conducteurService.findById(id));
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<ConducteurResponse>> search(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String email) {
        return ResponseEntity.ok(conducteurService.search(nom, email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConducteurResponse> update(@PathVariable Long id,
            @RequestBody @Valid ConducteurRequest request) {
        return ResponseEntity.ok(conducteurService.update(id, request));
    }
}
