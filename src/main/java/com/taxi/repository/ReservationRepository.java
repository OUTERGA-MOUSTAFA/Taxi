package com.taxi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taxi.entity.Reservation;
import com.taxi.enums.StatutReservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query ("select coalesce(sum(r.nombrePlaces), 0) " +
           "from Reservation r " +
           "where r.trajet.id = :trajetId " +
           "and r.statut = com.taxi.enums.StatutReservation.CONFIRMEE")
    Long sumPlacesConfirmees(@Param ("trajetId") Long trajetId);

    boolean existsByPassagerIdAndTrajetIdAndStatut(Long passagerId, Long trajetId, StatutReservation statut);

    List<Reservation> findByPassagerId(Long passagerId);

    List<Reservation> findByTrajetId(Long trajetId);
}
