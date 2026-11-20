package com.taxi.repository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.taxi.entity.Trajet;

@Repository
public interface TrajetRepository extends JpaRepository<Trajet, Long> {
    // List<Trajet> findByConducteurId(Long conducteurId);
    // boolean existsByVehiculeId(Long vehiculeId);

    // kelshi 3la dik distinct => repitation dyale les trajets ila kan ande les pointes d'arret mli kan khdeme b join fetch
    @Query("select distinct t from Trajet t " +
           "join fetch t.pointsArret " +
           "join fetch t.conducteur " +
           "join fetch t.vehicule " +
           "where t.id = :id")
    Optional<Trajet> findDetailById(@Param ("id") Long id);

    List<Trajet> findByConducteurId(Long conducteurId);

    boolean existsByVehiculeId(Long vehiculeId);
}
