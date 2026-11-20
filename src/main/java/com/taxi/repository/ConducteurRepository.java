package com.taxi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taxi.entity.Conducteur;
@Repository 
public interface ConducteurRepository extends JpaRepository<Conducteur, Long> {

    boolean existsByEmail(String email);

    boolean existsByNumeroPermis(String numeroPermis);

    List<Conducteur> findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(String nom, String email);

    List<Conducteur> findByNomContainingIgnoreCase(String nom);

    List<Conducteur> findByEmailContainingIgnoreCase(String email);

    List<Conducteur> findByNomContainingIgnoreCaseAndEmailContainingIgnoreCase(String nom, String email);
}
