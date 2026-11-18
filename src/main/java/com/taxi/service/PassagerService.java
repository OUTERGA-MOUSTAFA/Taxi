package com.taxi.service;

import java.util.List;

import com.taxi.dto.PassagerRequest;
import com.taxi.dto.PassagerResponse;

public interface PassagerService {
    PassagerResponse create(PassagerRequest request);
    List<PassagerResponse> findAll();
    PassagerResponse findById(Long id);
    PassagerResponse update(Long id, PassagerRequest request);
    List<PassagerResponse> searchByEmail(String email);
}
