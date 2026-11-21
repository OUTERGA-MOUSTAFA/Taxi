package com.taxi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taxi.entity.PointArret;

public interface PointArretRepository extends JpaRepository<PointArret, Long>{
    
}
