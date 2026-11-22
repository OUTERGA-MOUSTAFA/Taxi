package com.taxi.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.taxi.dto.PointArretRequest;
import com.taxi.dto.PointArretResponse;
import com.taxi.dto.ReservationResponse;
import com.taxi.dto.TrajetRechercheResponse;
import com.taxi.dto.TrajetRequest;
import com.taxi.dto.TrajetResponse;
import com.taxi.entity.Conducteur;
import com.taxi.entity.PointArret;
import com.taxi.entity.Reservation;
import com.taxi.entity.Trajet;
import com.taxi.entity.Vehicule;
import com.taxi.enums.StatutReservation;
import com.taxi.enums.StatutTrajet;
import com.taxi.exception.BadRequestException;
import com.taxi.exception.BusinessRuleException;
import com.taxi.exception.ForbiddenActionException;
import com.taxi.exception.ResourceNotFoundException;
import com.taxi.mapper.ReservationMapper;
import com.taxi.mapper.TrajetMapper;
import com.taxi.repository.ConducteurRepository;
import com.taxi.repository.ReservationRepository;
import com.taxi.repository.TrajetRepository;
import com.taxi.repository.VehiculeRepository;
import com.taxi.service.TrajetService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrajetServiceImpl implements TrajetService {
    private static final Logger log = LoggerFactory.getLogger(TrajetServiceImpl.class);

    private final TrajetRepository trajetRepository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final TrajetMapper trajetMapper;
    private final ReservationMapper reservationMapper;
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional
    public TrajetResponse publierTrajet(TrajetRequest request) {
        Conducteur conducteur = conducteurRepository.findById(request.conducteurId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conducteur non trouvé avec l'id: " + request.conducteurId()));

        Vehicule vehicule = vehiculeRepository.findById(request.vehiculeId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Véhicule non trouvé avec l'id: " + request.vehiculeId()));

        verifierProprietaireVehicule(conducteur, vehicule);

        List<PointArretRequest> points = request.pointsArret();
        verifierPoints(points);
        verifierHeures(points);
        verifierPrix(points);
        verifierPlaces(request.placesProposees(), vehicule);

        Trajet trajet = trajetMapper.toEntity(request);
        trajet.setConducteur(conducteur);
        trajet.setVehicule(vehicule);
        trajet.setStatut(StatutTrajet.PUBLIE);

        for (int i = 0; i < points.size(); i++) {
            PointArret point = trajetMapper.toPointEntity(points.get(i));
            point.setOrdre(i);
            trajet.addPointArret(point);
        }

        PointArretRequest premierPoint = points.get(0);// first point d'arret
        PointArretRequest dernierPoint = points.get(points.size() - 1);

        trajet.setVilleDepart(premierPoint.ville().trim()); // villeDepart
        trajet.setVilleArrivee(dernierPoint.ville().trim());// villeArrivee
        trajet.setDateDepart(premierPoint.heurePassage());// dateDepart

        // return trajetMapper.toResponse(saved);
        Trajet saved = trajetRepository.save(trajet);
        log.info("Trajet publié id={}", saved.getId());

        return buildTrajetResponse(saved);
    }

    private void verifierProprietaireVehicule(Conducteur conducteur, Vehicule vehicule) {
        if (!vehicule.getConducteur().getId().equals(conducteur.getId())) {
            log.warn("Règle refusée (403) : le véhicule id={} n'appartient pas au conducteur id={}",
                    vehicule.getId(), conducteur.getId());
            throw new ForbiddenActionException("Le véhicule spécifié n'appartient pas à ce conducteur.");
        }
    }

    private void verifierPoints(List<PointArretRequest> points) {// villeDepart
        if (points == null || points.size() < 2) {
            log.warn("Règle refusée (400) : un trajet doit comporter au moins 2 points d'arrêt");
            throw new BadRequestException("Un trajet doit contenir au moins 2 points d'arrêt.");
        }

        Set<String> villesVues = new HashSet<>();
        for (PointArretRequest point : points) {
            String villeNormalisee = point.ville().trim().toLowerCase();
            if (!villesVues.add(villeNormalisee)) {
                log.warn("Règle refusée (400) : ville en double détectée '{}'", point.ville());
                throw new BadRequestException(
                        "Chaque ville ne peut apparaître qu'une seule fois dans le trajet : " + point.ville());
            }
        }
    }

    private void verifierHeures(List<PointArretRequest> points) {
        LocalDateTime maintenant = LocalDateTime.now();
        if (!points.get(0).heurePassage().isAfter(maintenant)) {
            log.warn("Règle refusée (400) : l'heure de départ ({}) doit être dans le futur",
                    points.get(0).heurePassage());
            throw new BadRequestException("La date et l'heure de départ doivent être dans le futur.");
        }

        for (int i = 1; i < points.size(); i++) {
            LocalDateTime precedente = points.get(i - 1).heurePassage();
            LocalDateTime courante = points.get(i).heurePassage();
            if (!courante.isAfter(precedente)) {
                log.warn("Règle refusée (400) : heures non strictement croissantes entre l'ordre {} et {}", i - 1, i);
                throw new BadRequestException("Les heures de passage doivent être strictement croissantes.");
            }
        }
    }

    private void verifierPrix(List<PointArretRequest> points) {
        BigDecimal prixPremier = points.get(0).prixDepuisDepart();
        if (prixPremier.compareTo(BigDecimal.ZERO) != 0) {
            log.warn("Règle refusée (400) : le prix au premier point doit être égal à 0, reçu {}", prixPremier);
            throw new BadRequestException("Le prix au premier point de départ doit être égal à 0.");
        }

        for (int i = 1; i < points.size(); i++) {
            BigDecimal prixPrecedent = points.get(i - 1).prixDepuisDepart();
            BigDecimal prixCourant = points.get(i).prixDepuisDepart();
            if (prixCourant.compareTo(prixPrecedent) <= 0) {
                log.warn("Règle refusée (400) : prix non strictement croissant entre l'ordre {} et {}", i - 1, i);
                throw new BadRequestException("Les prix depuis le départ doivent être strictement croissants.");
            }
        }
    }

    private void verifierPlaces(int placesProposees, Vehicule vehicule) {
        if (placesProposees < 1 || placesProposees > vehicule.getCapacite()) {
            log.warn("Règle refusée (400) : places proposées ({}) hors limites [1..{}]",
                    placesProposees, vehicule.getCapacite());
            throw new BadRequestException(
                    "Le nombre de places proposées doit être compris entre 1 et la capacité du véhicule ("
                            + vehicule.getCapacite() + ").");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TrajetResponse findById(Long id) {
        Trajet trajet = trajetRepository.findDetailById(id)
                .orElseThrow(() -> {
                    log.warn("Trajet introuvable id={}", id);
                    return new ResourceNotFoundException("Trajet non trouvé avec l'id: " + id);
                });

        return buildTrajetResponse(trajet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrajetResponse> findByConducteurId(Long conducteurId) {
        if (!conducteurRepository.existsById(conducteurId)) {
            log.warn("Conducteur introuvable lors de la consultation des trajets id={}", conducteurId);
            throw new ResourceNotFoundException("Conducteur non trouvé avec l'id: " + conducteurId);
        }

        return trajetRepository.findDetailByConducteurId(conducteurId).stream() // hna fash kayn changement
                .map(this::buildTrajetResponse)
                .toList();
    }

    private int calculerPlacesRestantes(Trajet trajet) {
        Long confirmees = reservationRepository.sumPlacesConfirmees(trajet.getId());
        int totalConfirmees = confirmees != null ? confirmees.intValue() : 0;
        return Math.max(0, trajet.getPlacesProposees() - totalConfirmees);
    }

    private TrajetResponse buildTrajetResponse(Trajet trajet) {
        TrajetResponse base = trajetMapper.toResponse(trajet);
        List<PointArretResponse> pointsOrdonnes = trajet.getPointsArret().stream()
                .sorted((a, b) -> Integer.compare(a.getOrdre(), b.getOrdre()))
                .map(trajetMapper::toPointResponse)
                .toList();

        return new TrajetResponse(
                base.id(),
                base.conducteurId(),
                base.conducteurNom(),
                base.vehiculeId(),
                base.vehiculeImmatriculation(),
                base.villeDepart(),
                base.villeArrivee(),
                base.dateDepart(),
                base.placesProposees(),
                calculerPlacesRestantes(trajet),
                base.statut(),
                pointsOrdonnes);
    }

    @Override
    @Transactional
    public void terminerTrajet(Long id, Long conducteurId) {
        Trajet trajet = trajetRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Trajet introuvable lors de la tentative de clôture id={}", id);
                    return new ResourceNotFoundException("Trajet non trouvé avec l'id: " + id);
                });

        if (!trajet.getConducteur().getId().equals(conducteurId)) {
            log.warn(
                    "Règle refusée (403) : le conducteur id={} tente de terminer le trajet id={} appartenant au conducteur id={}",
                    conducteurId, id, trajet.getConducteur().getId());
            throw new ForbiddenActionException("Seul le conducteur propriétaire peut terminer ce trajet.");
        }

        if (trajet.getStatut() == StatutTrajet.TERMINE || trajet.getStatut() == StatutTrajet.ANNULE) {
            log.warn("Règle refusée (409) : impossible de terminer le trajet id={} avec le statut {}",
                    id, trajet.getStatut());
            throw new BusinessRuleException("Impossible de terminer un trajet déjà terminé ou annulé.");
        }

        if (!LocalDateTime.now().isAfter(trajet.getDateDepart())) {
            log.warn("Règle refusée (409) : impossible de terminer le trajet id={} avant l'heure de départ ({})",
                    id, trajet.getDateDepart());
            throw new BusinessRuleException("Impossible de terminer le trajet avant l'heure de départ.");
        }

        trajet.setStatut(StatutTrajet.TERMINE);
        trajetRepository.save(trajet);
        log.info("Trajet terminé id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TrajetRechercheResponse> rechercherTrajets(
            String villeDepart,
            String villeArrivee,
            LocalDate date,
            int places,
            BigDecimal prixMax,
            Pageable pageable) {

        if (villeDepart == null || villeDepart.isBlank()) {
            log.warn("Recherche refusée (400) : villeDepart manquante");
            throw new BadRequestException("La ville de départ est obligatoire.");
        }
        if (villeArrivee == null || villeArrivee.isBlank()) {
            log.warn("Recherche refusée (400) : villeArrivee manquante");
            throw new BadRequestException("La ville d'arrivée est obligatoire.");
        }
        if (date == null) {
            log.warn("Recherche refusée (400) : date manquante");
            throw new BadRequestException("La date de recherche est obligatoire.");
        }
        if (places < 1) {
            log.warn("Recherche refusée (400) : nombre de places invalide ({})", places);
            throw new BadRequestException("Le nombre de places doit être supérieur ou égal à 1.");
        }
        if (prixMax != null && prixMax.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Recherche refusée (400) : prixMax négatif ({})", prixMax);
            throw new BadRequestException("Le prix maximum ne peut pas être négatif.");
        }

        LocalDateTime debutJour = date.atStartOfDay();
        LocalDateTime finJour = date.plusDays(1).atStartOfDay();

        Page<TrajetRechercheResponse> resultats = trajetRepository.rechercherTrajets(
                villeDepart.trim(),
                villeArrivee.trim(),
                debutJour,
                finJour,
                places,
                prixMax,
                StatutTrajet.PUBLIE,
                pageable);

        log.info("Recherche trajets {} -> {} le {} (places={}) : {} résultat(s)",
                villeDepart.trim(), villeArrivee.trim(), date, places, resultats.getTotalElements());

        return resultats;
    }

    // conducteur Anuller trajet donc khas idar rollback l reservations
    @Override
    @Transactional
    public void annulerTrajet(Long id, Long conducteurId) {
        Trajet trajet = trajetRepository.findById(id)// 9eleb 3la Trajet
                .orElseThrow(() -> {
                    log.warn("Annulation trajet refusée (404) : trajet introuvable id={}", id);
                    return new ResourceNotFoundException("Trajet non trouvé avec l'id: " + id);
                });

        if (!trajet.getConducteur().getId().equals(conducteurId)) {// check wash trajet dyal had conducteur id
            log.warn(
                    "Règle refusée (403) : conducteur id={} tente d'annuler le trajet id={} appartenant au conducteur id={}",
                    conducteurId, id, trajet.getConducteur().getId());
            throw new ForbiddenActionException("Seul le conducteur propriétaire peut annuler ce trajet.");
        }

        // check wash TERMINE pas Anuler ila kant businessRulleException
        if (trajet.getStatut() == StatutTrajet.TERMINE || trajet.getStatut() == StatutTrajet.ANNULE) {
            log.warn("Règle refusée (409) : impossible d'annuler le trajet id={} avec le statut {}",
                    id, trajet.getStatut());
            throw new BusinessRuleException("Impossible d'annuler un trajet déjà terminé ou annulé.");
        }

        trajet.setStatut(StatutTrajet.ANNULE);// hna trajet kanbdleh l annuler
        trajetRepository.save(trajet);

        List<Reservation> confirmees = reservationRepository.findByTrajetId(id).stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)// kanjib le reservations li statut dyalhem
                                                                          // Confirmee
                .toList();

        confirmees.forEach(r -> r.setStatut(StatutReservation.ANNULEE)); // kanbdel status dyale reservation l Annulee

        if (!confirmees.isEmpty()) {
            reservationRepository.saveAll(confirmees); // trajet o reservations managed f @Transactional hibernet kaydir
                                                       // UPDATE
        }

        log.info("Trajet annulé id={} par conducteur id={} ; {} réservation(s) passée(s) à ANNULEE",
                id, conducteurId, confirmees.size());
    }

    // consulté les reservations dyale trajet x
    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> findReservationsByTrajetId(Long trajetId, Long conducteurId) {
        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> {
                    log.warn("Consultation réservations refusée (404) : trajet introuvable id={}", trajetId);
                    return new ResourceNotFoundException("Trajet non trouvé avec l'id: " + trajetId);
                });

        if (!trajet.getConducteur().getId().equals(conducteurId)) {
            log.warn(
                    "Règle refusée (403) : conducteur id={} tente de consulter les réservations du trajet id={} appartenant au conducteur id={}",
                    conducteurId, trajetId, trajet.getConducteur().getId());
            throw new ForbiddenActionException(
                    "Seul le conducteur propriétaire peut consulter les réservations de ce trajet.");
        }

        List<ReservationResponse> resultats = reservationRepository.findDetailByTrajetId(trajetId).stream()
                .map(reservationMapper::toResponse)
                .toList();

        log.info("Consultation réservations trajet id={} par conducteur id={} : {} réservation(s)",
                trajetId, conducteurId, resultats.size());

        return resultats;
    }
}
