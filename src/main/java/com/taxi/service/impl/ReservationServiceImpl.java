package com.taxi.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taxi.dto.ReservationRequest;
import com.taxi.dto.ReservationResponse;
import com.taxi.entity.Passager;
import com.taxi.entity.PointArret;
import com.taxi.entity.Reservation;
import com.taxi.entity.Trajet;
import com.taxi.enums.StatutReservation;
import com.taxi.enums.StatutTrajet;
import com.taxi.exception.BadRequestException;
import com.taxi.exception.BusinessRuleException;
import com.taxi.exception.ResourceNotFoundException;
import com.taxi.mapper.ReservationMapper;
import com.taxi.repository.PassagerRepository;
import com.taxi.repository.PointArretRepository;
import com.taxi.repository.ReservationRepository;
import com.taxi.repository.TrajetRepository;
import com.taxi.service.ReservationService;

@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final PassagerRepository passagerRepository;
    private final TrajetRepository trajetRepository;
    private final PointArretRepository pointArretRepository;
    private final ReservationMapper reservationMapper;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
            PassagerRepository passagerRepository,
            TrajetRepository trajetRepository, PointArretRepository pointArretRepository,
            ReservationMapper reservationMapper) {
        this.passagerRepository = passagerRepository;
        this.trajetRepository = trajetRepository;
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.pointArretRepository = pointArretRepository;

    }

    @Override
    @Transactional
    public ReservationResponse reserver(ReservationRequest request) {
        Passager passager = passagerRepository.findById(request.passagerId())
                .orElseThrow(() -> {
                    log.warn("Réservation refusée (404) : passager introuvable id={}", request.passagerId());
                    return new ResourceNotFoundException("Passager non trouvé avec l'id: " + request.passagerId());
                });

        Trajet trajet = trajetRepository.findById(request.trajetId())
                .orElseThrow(() -> {
                    log.warn("Réservation refusée (404) : trajet introuvable id={}", request.trajetId());
                    return new ResourceNotFoundException("Trajet non trouvé avec l'id: " + request.trajetId());
                });

        PointArret pointMontee = pointArretRepository.findById(request.pointMonteeId())
                .orElseThrow(() -> {
                    log.warn("Réservation refusée (404) : point de montée introuvable id={}", request.pointMonteeId());
                    return new ResourceNotFoundException(
                            "Point de montée non trouvé avec l'id: " + request.pointMonteeId());
                });

        PointArret pointDescente = pointArretRepository.findById(request.pointDescenteId())
                .orElseThrow(() -> {
                    log.warn("Réservation refusée (404) : point de descente introuvable id={}",
                            request.pointDescenteId());
                    return new ResourceNotFoundException(
                            "Point de descente non trouvé avec l'id: " + request.pointDescenteId());
                });

                // kela wehda 3endha warn dyalha, 9a3ida 1 l kela méthode
        verifierAppartenancePoints(trajet, pointMontee, pointDescente);
        verifierOrdrePoints(pointMontee, pointDescente);
        verifierStatutTrajet(trajet);
        verifierDoublonReservation(passager.getId(), trajet.getId());
        verifierPlacesDisponibles(trajet, request.nombrePlaces());

        BigDecimal montantTotal = calculerMontant(pointMontee, pointDescente, request.nombrePlaces());// Calcul du montant pointMontee, pointDescente, places

        Reservation reservation = reservationMapper.toEntity(request);
        reservation.setPassager(passager);
        reservation.setTrajet(trajet);
        reservation.setPointMontee(pointMontee);
        reservation.setPointDescente(pointDescente);
        // reservation.setNombrePlaces(request.nombrePlaces()); hadi blash mapper kaytkelef
        reservation.setMontantTotal(montantTotal);
        reservation.setStatut(StatutReservation.CONFIRMEE);

        Reservation saved = reservationRepository.save(reservation);
        mettreAJourStatut(trajet);// COMPLET si 0 place, PUBLIE sinon (réutilisée à l'annulation).
        trajetRepository.save(trajet);

        log.info("Réservation créée id={} pour passager id={} sur trajet id={} (montant={} DH)",
                saved.getId(), passager.getId(), trajet.getId(), saved.getMontantTotal());

        return reservationMapper.toResponse(saved);
    }

    private void verifierAppartenancePoints(Trajet trajet, PointArret pointMontee, PointArret pointDescente) {
        if (!pointMontee.getTrajet().getId().equals(trajet.getId())
                || !pointDescente.getTrajet().getId().equals(trajet.getId())) {
            log.warn("Règle refusée (400) : points d'arrêt ({}, {}) n'appartiennent pas au trajet id={}",
                    pointMontee.getId(), pointDescente.getId(), trajet.getId());
            throw new BadRequestException("Les points de montée et de descente doivent appartenir au trajet spécifié.");
        }
    }

    private void verifierOrdrePoints(PointArret pointMontee, PointArret pointDescente) {
        if (pointMontee.getOrdre() >= pointDescente.getOrdre()) {
            log.warn("Règle refusée (400) : ordre invalide montée ({}) >= descente ({})",
                    pointMontee.getOrdre(), pointDescente.getOrdre());
            throw new BadRequestException("Le point de montée doit précéder le point de descente dans le trajet.");
        }
    }

    private void verifierStatutTrajet(Trajet trajet) {
        if (trajet.getStatut() != StatutTrajet.PUBLIE) {
            log.warn("Règle refusée (409) : trajet id={} non PUBLIE (statut actuel : {})",
                    trajet.getId(), trajet.getStatut());
            throw new BusinessRuleException("La réservation n'est possible que sur un trajet publié.");
        }
    }

    private void verifierDoublonReservation(Long passagerId, Long trajetId) {
        if (reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(
                passagerId, trajetId, StatutReservation.CONFIRMEE)) {
            log.warn("Règle refusée (409) : passager id={} a déjà une réservation CONFIRMEE sur trajet id={}",
                    passagerId, trajetId);
            throw new BusinessRuleException("Vous avez déjà une réservation confirmée sur ce trajet.");
        }
    }

    private void verifierPlacesDisponibles(Trajet trajet, int nombrePlacesDemandees) {
        int placesRestantes = calculerPlacesRestantes(trajet);
        if (placesRestantes < nombrePlacesDemandees) {
            log.warn("Règle refusée (409) : places insuffisantes sur trajet id={} (demandées={}, restantes={})",
                    trajet.getId(), nombrePlacesDemandees, placesRestantes);
            throw new BusinessRuleException("Nombre de places restantes insuffisant sur ce trajet.");
        }
    }

    public BigDecimal calculerMontant(PointArret pointMontee, PointArret pointDescente, int nombrePlaces) {
        BigDecimal prixSegment = pointDescente.getPrixDepuisDepart().subtract(pointMontee.getPrixDepuisDepart());
        return prixSegment
                .multiply(BigDecimal.valueOf(nombrePlaces))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // COMPLET si 0 place, PUBLIE sinon (réutilisée à l'annulation).
    public void mettreAJourStatut(Trajet trajet) {
        if (trajet.getStatut() == StatutTrajet.TERMINE || trajet.getStatut() == StatutTrajet.ANNULE) {
            return;
        }
        int placesRestantes = calculerPlacesRestantes(trajet);
        if (placesRestantes <= 0) {
            trajet.setStatut(StatutTrajet.COMPLET);
        } else {
            trajet.setStatut(StatutTrajet.PUBLIE);
        }
    }

    private int calculerPlacesRestantes(Trajet trajet) {
        Long confirmees = reservationRepository.sumPlacesConfirmees(trajet.getId());
        int totalConfirmees = confirmees != null ? confirmees.intValue() : 0;
        return Math.max(0, trajet.getPlacesProposees() - totalConfirmees);
    }
}
