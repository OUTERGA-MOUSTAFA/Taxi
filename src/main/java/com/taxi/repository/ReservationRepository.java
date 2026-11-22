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

    @Query("select coalesce(sum(r.nombrePlaces), 0) " +
            "from Reservation r " +
            "where r.trajet.id = :trajetId " +
            "and r.statut = com.taxi.enums.StatutReservation.CONFIRMEE")
    Long sumPlacesConfirmees(@Param("trajetId") Long trajetId);

    boolean existsByPassagerIdAndTrajetIdAndStatut(Long passagerId, Long trajetId, StatutReservation statut);

    List<Reservation> findByTrajetId(Long trajetId);

    // List<Reservation> findByPassagerId(Long passagerId);//=> hadi katjib trajet
    // 3ad tmshi tjib pointDescente apre tmshi tjib pointMontee
    // كيوليو 2 طلبات بلاصت 7
    @Query("select r from Reservation r " +
            "join fetch r.passager " +
            "join fetch r.trajet " +
            "join fetch r.pointMontee " +
            "join fetch r.pointDescente " +
            "where r.passager.id = :passagerId " +
            "order by r.dateReservation desc")
    List<Reservation> findDetailByPassagerId(@Param("passagerId") Long passagerId);

    @Query("select r from Reservation r " +
            "join fetch r.passager " +
            "join fetch r.pointMontee " +
            "join fetch r.pointDescente " +
            "where r.trajet.id = :trajetId " +
            "order by r.dateReservation")
    List<Reservation> findDetailByTrajetId(@Param("trajetId") Long trajetId);// dert hadi hit ghadir liya findByTrajetId(khlitha l annulerTrajet)
                                                                             // ila khdemt biha f method
                                                                             // findReservationsByTrajetId N + 1: kela reservation jib passager jib demande les pointArret
}
