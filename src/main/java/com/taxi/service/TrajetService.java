package com.taxi.service;

import java.util.List;
import com.taxi.dto.TrajetRequest;
import com.taxi.dto.TrajetResponse;

public interface TrajetService {

    TrajetResponse publierTrajet(TrajetRequest request);

    TrajetResponse findById(Long id);
    List<TrajetResponse> findByConducteurId(Long conducteurId);

    void terminerTrajet(Long id, Long conducteurId);
}
