package com.taxi.service;

import java.util.List;

import com.taxi.dto.ConducteurRequest;
import com.taxi.dto.ConducteurResponse;

public interface ConducteurService {

    ConducteurResponse create(ConducteurRequest request);
    List<ConducteurResponse> findAll();
    ConducteurResponse findById(Long id);
    ConducteurResponse update(Long id, ConducteurRequest request);
    List<ConducteurResponse> search(String nom, String email);
}
