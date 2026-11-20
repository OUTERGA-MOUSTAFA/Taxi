package com.taxi.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.taxi.entity.Trajet;

@Repository
public interface TrajetRepository extends JpaRepository<Trajet, Long> {
    List<Trajet> findByConducteurId(Long conducteurId);

    boolean existsByVehiculeId(Long vehiculeId);
}
