package com.taxi.repository;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.taxi.dto.TrajetRechercheResponse;
import com.taxi.entity.Trajet;
import com.taxi.enums.StatutTrajet;

@Repository
public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    // List<Trajet> findByConducteurId(Long conducteurId);
    // boolean existsByVehiculeId(Long vehiculeId);

    // Détail d'un trajet en une seule requête
    // kelshi 3l a dik distinct=> repitation dyale les trajets ila kan ande les
    // pointes d'arret mli kan khdeme b join fetch
    @Query("select distinct t from Trajet t " + "join fetch t.pointsArret " + "join fetch t.conducteur "
            + "join fetch t.vehicule " + "where t.id = :id")

    Optional<Trajet> findDetailById(@Param("id") Long id);

    // Trajets d'un conducteur sans N+1
    @Query("select distinct t from Trajet t " +
            "join fetch t.pointsArret " +
            "join fetch t.conducteur " +
            "join fetch t.vehicule " +
            "where t.conducteur.id = :conducteurId " +
            "order by t.dateDepart")
    List<Trajet> findDetailByConducteurId(@Param("conducteurId") Long conducteurId);

    List<Trajet> findByConducteurId(Long conducteurId);

    boolean existsByVehiculeId(Long vehiculeId);


    @Query(
        value = "select new com.taxi.dto.TrajetRechercheResponse(" +
                "  t.id, " +
                "  pm.ville, " +
                "  pm.heurePassage, " +// date du départ
                "  pd.ville, " +
                "  pd.heurePassage, " +
                "  (pd.prixDepuisDepart - pm.prixDepuisDepart), " +
                "  (t.placesProposees - coalesce((select sum(r.nombrePlaces) from Reservation r where r.trajet = t and r.statut = com.taxi.enums.StatutReservation.CONFIRMEE), 0)), " +
                "  c.nom" +
                ") " +
                "from Trajet t " +
                "join t.pointsArret pm " +
                "join t.pointsArret pd " +
                "join t.conducteur c " +
                "where pm.ordre < pd.ordre " +
                "  and lower(pm.ville) = lower(:villeDepart) " +
                "  and lower(pd.ville) = lower(:villeArrivee) " +
                "  and t.statut = :statut " +
                "  and pm.heurePassage >= :debutJour and pm.heurePassage < :finJour " + //date du départ b seway3 o nhar
                "and (cast(t.placesProposees as Long)) >= :places"+
                // "  and (t.placesProposees - coalesce((select sum(r.nombrePlaces) from Reservation r where r.trajet = t and r.statut = com.taxi.enums.StatutReservation.CONFIRMEE), 0)) >= :places " +
                "  and (:prixMax is null or (pd.prixDepuisDepart - pm.prixDepuisDepart) <= :prixMax) " +
                "order by t.dateDepart asc",
        countQuery = "select count(t) " +
                     "from Trajet t " +
                     "join t.pointsArret pm " +
                     "join t.pointsArret pd " +
                     "where pm.ordre < pd.ordre " +
                     "  and lower(pm.ville) = lower(:villeDepart) " +
                     "  and lower(pd.ville) = lower(:villeArrivee) " +
                     "  and t.statut = :statut " +
                     "  and pm.heurePassage >= :debutJour and pm.heurePassage < :finJour " +
                     "  and (t.placesProposees - coalesce((select sum(r.nombrePlaces) from Reservation r where r.trajet = t and r.statut = com.taxi.enums.StatutReservation.CONFIRMEE), 0)) >= :places " +
                     "  and (:prixMax is null or (pd.prixDepuisDepart - pm.prixDepuisDepart) <= :prixMax)"
    )
    Page<TrajetRechercheResponse> rechercherTrajets(
            @Param("villeDepart") String villeDepart,
            @Param("villeArrivee") String villeArrivee,
            @Param("debutJour") LocalDateTime debutJour,
            @Param("finJour") LocalDateTime finJour,
            @Param("places") int places,
            @Param("prixMax") BigDecimal prixMax,
            @Param("statut") StatutTrajet statut,
            Pageable pageable
    );
}
