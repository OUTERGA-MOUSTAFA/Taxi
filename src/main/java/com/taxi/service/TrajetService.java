package com.taxi.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.taxi.dto.TrajetRechercheResponse;
import com.taxi.dto.TrajetRequest;
import com.taxi.dto.TrajetResponse;

public interface TrajetService {

    TrajetResponse publierTrajet(TrajetRequest request);

    TrajetResponse findById(Long id);

    List<TrajetResponse> findByConducteurId(Long conducteurId);

    void terminerTrajet(Long id, Long conducteurId);

    Page<TrajetRechercheResponse> rechercherTrajets(
            String villeDepart,
            String villeArrivee,
            LocalDate date,
            int places,
            BigDecimal prixMax,
            Pageable pageable);

    // hada condusteur li kaydire
    void annulerTrajet(Long id, Long conducteurId);
}
