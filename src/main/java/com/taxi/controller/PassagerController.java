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
import com.taxi.service.PassagerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping ("/api/passagers")
@RequiredArgsConstructor
public class PassagerController {
    private final PassagerService passagerService;

    // public PassagerController(PassagerService passagerService){
    //     this.passagerService = passagerService;
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
}
