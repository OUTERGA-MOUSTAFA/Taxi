package com.taxi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taxi.dto.TrajetRequest;
import com.taxi.dto.TrajetResponse;
import com.taxi.service.TrajetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping ("/api/trajets")
@RequiredArgsConstructor
public class TrajetController {

    private final TrajetService trajetService;

    @PostMapping 
    public ResponseEntity<TrajetResponse> publierTrajet(@RequestBody @Valid TrajetRequest request) {
        TrajetResponse response = trajetService.publierTrajet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}