package com.taxi.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.taxi.dto.PointArretRequest;
import com.taxi.dto.PointArretResponse;
import com.taxi.dto.TrajetRequest;
import com.taxi.dto.TrajetResponse;
import com.taxi.entity.Conducteur;
import com.taxi.entity.PointArret;
import com.taxi.entity.Trajet;
import com.taxi.entity.Vehicule;
import com.taxi.enums.StatutTrajet;
import com.taxi.exception.BadRequestException;
import com.taxi.exception.ForbiddenActionException;
import com.taxi.exception.ResourceNotFoundException;
import com.taxi.mapper.TrajetMapper;
import com.taxi.repository.ConducteurRepository;
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

        PointArretRequest premierPoint = points.get(0);// firt point d'arret
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
        // E6: remplacer par :
        // int placesReservees =
        // reservationRepository.sumPlacesByTrajetIdAndStatut(trajet.getId(),
        // StatutReservation.CONFIRMEE);
        // return Math.max(0, trajet.getPlacesProposees() - placesReservees);
        return trajet.getPlacesProposees();
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
}
