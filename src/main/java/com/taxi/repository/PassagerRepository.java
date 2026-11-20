package com.taxi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taxi.entity.Passager;
@Repository 
public interface PassagerRepository extends JpaRepository<Passager ,Long>{
    boolean existsByEmail(String email);
    List<Passager> findByEmailContainingIgnoreCase(String email);
}
