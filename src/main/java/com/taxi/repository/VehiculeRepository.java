package com.taxi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taxi.entity.Vehicule;

public interface VehiculeRepository extends  JpaRepository <Vehicule, Long>{
    
    List<Vehicule> findByConducteurId(Long conducteurId);
    boolean existsByImmatriculation(String immatriculation);
}
