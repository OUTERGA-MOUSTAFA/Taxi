package com.taxi.service;

import java.util.List;

import com.taxi.dto.VehiculeRequest;
import com.taxi.dto.VehiculeResponse;

public interface VehiculeService {
    VehiculeResponse create(VehiculeRequest request);

    VehiculeResponse findById(Long id);

    VehiculeResponse update(Long id, VehiculeRequest request);

    List<VehiculeResponse> findByConducteurId(Long conducteurId);

    void delete(Long id);
}
