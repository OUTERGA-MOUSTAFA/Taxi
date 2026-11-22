package com.taxi.service.impl;

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
import com.taxi.exception.BusinessRuleException;
import com.taxi.exception.ForbiddenActionException;
import com.taxi.mapper.ReservationMapper;
import com.taxi.mapper.TrajetMapper;
import com.taxi.repository.ConducteurRepository;
import com.taxi.repository.ReservationRepository;
import com.taxi.repository.TrajetRepository;
import com.taxi.repository.VehiculeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrajetServiceImplTest {

    @Mock
    private TrajetRepository trajetRepository;

    @Mock
    private ConducteurRepository conducteurRepository;

    @Mock
    private VehiculeRepository vehiculeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TrajetMapper trajetMapper;

    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private TrajetServiceImpl trajetService;

    // --- S7.1.1 : Fixture du trajet de Hamid ---

    private Conducteur conducteurHamid() {
        return Conducteur.builder()
                .id(1L)
                .nom("Hamid")
                .build();
    }

    private Conducteur autreConducteur() {
        return Conducteur.builder()
                .id(2L)
                .nom("Youssef")
                .build();
    }

    private Vehicule vehiculeCapacite4(Conducteur proprietaire) {
        return Vehicule.builder()
                .id(10L)
                .immatriculation("12345-A-1")
                .capacite(4)
                .conducteur(proprietaire)
                .build();
    }

    private List<PointArretRequest> quatrePointsValides() {// plusDays(2) f future bash idez test mezyan
        LocalDateTime base = LocalDateTime.now().plusDays(2).withHour(8).withMinute(0).withSecond(0).withNano(0);
        return List.of(
                new PointArretRequest("Rabat", base, BigDecimal.valueOf(0.00)),
                new PointArretRequest("Casablanca", base.plusHours(1).plusMinutes(30), BigDecimal.valueOf(40.00)),
                new PointArretRequest("Settat", base.plusHours(2).plusMinutes(45), BigDecimal.valueOf(70.00)),
                new PointArretRequest("Marrakech", base.plusHours(5), BigDecimal.valueOf(120.00)));
    }

    private TrajetRequest requeteHamidValide() {
        return new TrajetRequest(1L, 10L, 3, quatrePointsValides());
    }

    private void stubMappingPourCreation(TrajetRequest request, Conducteur conducteur, Vehicule vehicule) {
        Trajet mappedTrajet = Trajet.builder()
                .conducteur(conducteur)
                .vehicule(vehicule)
                .placesProposees(request.placesProposees())
                .pointsArret(new ArrayList<>())
                .build();

        when(conducteurRepository.findById(conducteur.getId())).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(vehicule.getId())).thenReturn(Optional.of(vehicule));
        when(trajetMapper.toEntity(request)).thenReturn(mappedTrajet);// tayakhd les valeurs normal donc manhtashj mock

        when(trajetMapper.toPointEntity(any(PointArretRequest.class))).thenAnswer(inv -> {
            PointArretRequest p = inv.getArgument(0);
            return PointArret.builder()
                    .ville(p.ville())
                    .heurePassage(p.heurePassage())
                    .prixDepuisDepart(p.prixDepuisDepart())
                    .build();
        });

        when(trajetRepository.save(any(Trajet.class))).thenAnswer(inv -> {
            Trajet t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });

        when(trajetMapper.toResponse(any(Trajet.class))).thenAnswer(inv -> {
            Trajet t = inv.getArgument(0);
            List<PointArretResponse> pts = t.getPointsArret().stream()
                    .map(pa -> new PointArretResponse(pa.getId(), pa.getVille(), pa.getOrdre(), pa.getHeurePassage(),
                            pa.getPrixDepuisDepart()))
                    .toList();
            return new TrajetResponse(
                    t.getId(),
                    t.getConducteur().getId(),
                    t.getConducteur().getNom(),
                    t.getVehicule().getId(),
                    t.getVehicule().getImmatriculation(),
                    t.getVilleDepart(),
                    t.getVilleArrivee(),
                    t.getDateDepart(),
                    t.getPlacesProposees(),
                    t.getPlacesProposees(),
                    t.getStatut(),
                    pts);
        });
    }

    // : Cas nominal: publierTrajet Creer Trajet En Statut Publie

    @Test
    void publierTrajet_nominal_doitCreerTrajetEnStatutPublie() {
        Conducteur hamid = conducteurHamid();
        Vehicule vehicule = vehiculeCapacite4(hamid);
        TrajetRequest request = requeteHamidValide();
        stubMappingPourCreation(request, hamid, vehicule);

        TrajetResponse response = trajetService.publierTrajet(request);

        assertThat(response.statut()).isEqualTo(StatutTrajet.PUBLIE);
        assertThat(response.villeDepart()).isEqualTo("Rabat");
        assertThat(response.villeArrivee()).isEqualTo("Marrakech");
        assertThat(response.pointsArret()).hasSize(4);
        verify(trajetRepository).save(any(Trajet.class));
    }

    // cas nominal: dyale terminerTrajet statut = TERMINE 
    @Test
    void terminerTrajet_apresHeureDepart_doitPasserEnStatutTermine() {
        Conducteur hamid = conducteurHamid();
        Vehicule vehicule = vehiculeCapacite4(hamid);
        Trajet trajetPasse = Trajet.builder()
                .id(60L)
                .conducteur(hamid)
                .vehicule(vehicule)
                .dateDepart(LocalDateTime.now().minusHours(2))
                .statut(StatutTrajet.PUBLIE)
                .build();

        when(trajetRepository.findById(60L)).thenReturn(Optional.of(trajetPasse));

        trajetService.terminerTrajet(60L, 1L);

        assertThat(trajetPasse.getStatut()).isEqualTo(StatutTrajet.TERMINE);
        verify(trajetRepository).save(trajetPasse);
    }

    // --- S7.1.3 : Règles refusées ---

    @Test
    void publierTrajet_5PlacesPourCapacite4_doitLeverExceptionSansSauvegarder() {
        Conducteur hamid = conducteurHamid();
        Vehicule vehicule = vehiculeCapacite4(hamid);
        TrajetRequest request = new TrajetRequest(1L, 10L, 5, quatrePointsValides());

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(hamid));
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> trajetService.publierTrajet(request))
                .isInstanceOf(BadRequestException.class);

        verify(trajetRepository, never()).save(any());
    }

    @Test
    void publierTrajet_vehiculeAutreConducteur_doitLeverForbiddenActionException() {
        Conducteur hamid = conducteurHamid();
        Conducteur karim = autreConducteur();
        Vehicule vehiculeDeKarim = vehiculeCapacite4(karim);
        TrajetRequest request = requeteHamidValide();

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(hamid));
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(vehiculeDeKarim));

        assertThatThrownBy(() -> trajetService.publierTrajet(request))
                .isInstanceOf(ForbiddenActionException.class);

        verify(trajetRepository, never()).save(any());
    }

    @Test
    void publierTrajet_heuresNonCroissantes_doitLeverExceptionSansSauvegarder() {
        Conducteur hamid = conducteurHamid();
        Vehicule vehicule = vehiculeCapacite4(hamid);
        LocalDateTime base = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);

        List<PointArretRequest> pointsInvalides = List.of(
                new PointArretRequest("Rabat", base, BigDecimal.ZERO),
                new PointArretRequest("Casablanca", base.minusMinutes(15), BigDecimal.valueOf(40)),
                new PointArretRequest("Marrakech", base.plusHours(4), BigDecimal.valueOf(120)));
        TrajetRequest request = new TrajetRequest(1L, 10L, 2, pointsInvalides);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(hamid));
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> trajetService.publierTrajet(request))
                .isInstanceOf(BadRequestException.class);

        verify(trajetRepository, never()).save(any());
    }

    @Test
    void publierTrajet_prixNonCroissant_doitLeverExceptionSansSauvegarder() {
        Conducteur hamid = conducteurHamid();
        Vehicule vehicule = vehiculeCapacite4(hamid);
        LocalDateTime base = LocalDateTime.now().plusDays(2).withHour(8).withMinute(0);

        List<PointArretRequest> pointsPrixInvalide = List.of(
                new PointArretRequest("Rabat", base, BigDecimal.ZERO),
                new PointArretRequest("Casablanca", base.plusHours(1), BigDecimal.valueOf(50)),
                new PointArretRequest("Marrakech", base.plusHours(3), BigDecimal.valueOf(45)));
        TrajetRequest request = new TrajetRequest(1L, 10L, 2, pointsPrixInvalide);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(hamid));
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> trajetService.publierTrajet(request))
                .isInstanceOf(BadRequestException.class);

        verify(trajetRepository, never()).save(any());
    }

    @Test
    void terminerTrajet_avantHeureDepart_doitLeverBusinessRuleException() {
        Conducteur hamid = conducteurHamid();
        Vehicule vehicule = vehiculeCapacite4(hamid);
        Trajet trajetFutur = Trajet.builder()
                .id(50L)
                .conducteur(hamid)
                .vehicule(vehicule)
                .dateDepart(LocalDateTime.now().plusHours(4))
                .statut(StatutTrajet.PUBLIE)
                .build();

        when(trajetRepository.findById(50L)).thenReturn(Optional.of(trajetFutur));

        assertThatThrownBy(() -> trajetService.terminerTrajet(50L, 1L))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(trajetFutur.getStatut()).isEqualTo(StatutTrajet.PUBLIE);
        verify(trajetRepository, never()).save(any());
    }
}