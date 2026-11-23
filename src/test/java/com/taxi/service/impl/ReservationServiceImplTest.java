package com.taxi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.taxi.mapper.ReservationMapper;
import com.taxi.repository.PassagerRepository;
import com.taxi.repository.PointArretRepository;
import com.taxi.repository.ReservationRepository;
import com.taxi.repository.TrajetRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PassagerRepository passagerRepository;

    @Mock
    private TrajetRepository trajetRepository;

    @Mock
    private PointArretRepository pointArretRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Passager passager(Long id, String nom) {
        return Passager.builder().id(id).nom(nom).build();
    }

    private Trajet trajetPublie(Long id, int placesProposees, LocalDateTime dateDepart) {
        return Trajet.builder()
                .id(id)
                .placesProposees(placesProposees)
                .dateDepart(dateDepart)
                .statut(StatutTrajet.PUBLIE)
                .build();
    }

    private PointArret point(Long id, Trajet trajet, String ville, int ordre, LocalDateTime heure, String prix) {
        return PointArret.builder()
                .id(id)
                .trajet(trajet)
                .ville(ville)
                .ordre(ordre)
                .heurePassage(heure)
                .prixDepuisDepart(new BigDecimal(prix))
                .build();
    }

    private void stubMapperBasique(ReservationRequest request, Passager passager, Trajet trajet,
            PointArret montee, PointArret descente) {
        when(reservationMapper.toEntity(request)).thenAnswer(inv -> Reservation.builder().build());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            r.setId(500L);
            return r;
        });
        when(reservationMapper.toResponse(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            return new ReservationResponse(
                    r.getId(),
                    passager.getId(),
                    passager.getNom(),
                    trajet.getId(),
                    montee.getId(),
                    montee.getVille(),
                    descente.getId(),
                    descente.getVille(),
                    r.getNombrePlaces(),
                    r.getMontantTotal(),
                    LocalDateTime.now(),
                    r.getStatut());
        });
    }

    @Test
    void reserver_settatVersMarrakech1Place_doitCalculer50DH() {
        LocalDateTime depart = LocalDateTime.now().plusDays(3);
        Passager salma = passager(1L, "Salma");
        Trajet trajet = trajetPublie(10L, 3, depart);
        PointArret settat = point(22L, trajet, "Settat", 2, depart.plusHours(2), "70.00");
        PointArret marrakech = point(23L, trajet, "Marrakech", 3, depart.plusHours(4), "120.00");

        ReservationRequest request = new ReservationRequest(1L, 10L, 22L, 23L, 1);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(salma));
        when(trajetRepository.findById(10L)).thenReturn(Optional.of(trajet));
        when(pointArretRepository.findById(22L)).thenReturn(Optional.of(settat));
        when(pointArretRepository.findById(23L)).thenReturn(Optional.of(marrakech));
        when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(1L, 10L, StatutReservation.CONFIRMEE))
                .thenReturn(false);
        // Avant sauvegarde : 0 place confirmée ; après sauvegarde (mockée via
        // sumPlacesConfirmees = 1L) : 2 places restantes
        when(reservationRepository.sumPlacesConfirmees(10L)).thenReturn(0L, 1L);
        stubMapperBasique(request, salma, trajet, settat, marrakech);

        ReservationResponse response = reservationService.reserver(request);

        assertThat(response.montantTotal()).isEqualByComparingTo("50.00");
        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.PUBLIE);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void reserver_casablancaVersMarrakech2Places_doitCalculer160DHetPasserTrajetEnComplet() {
        LocalDateTime depart = LocalDateTime.now().plusDays(3);
        Passager karim = passager(2L, "Karim");
        Trajet trajet = trajetPublie(10L, 2, depart);
        PointArret casa = point(21L, trajet, "Casablanca", 1, depart.plusHours(1), "40.00");
        PointArret marrakech = point(23L, trajet, "Marrakech", 3, depart.plusHours(4), "120.00");

        ReservationRequest request = new ReservationRequest(2L, 10L, 21L, 23L, 2);

        when(passagerRepository.findById(2L)).thenReturn(Optional.of(karim));
        when(trajetRepository.findById(10L)).thenReturn(Optional.of(trajet));
        when(pointArretRepository.findById(21L)).thenReturn(Optional.of(casa));
        when(pointArretRepository.findById(23L)).thenReturn(Optional.of(marrakech));
        when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(2L, 10L, StatutReservation.CONFIRMEE))
                .thenReturn(false);
        // Avant sauvegarde : 0 place ; après sauvegarde : 2 places confirmées sur 2 =>
        // 0 restante => COMPLET
        when(reservationRepository.sumPlacesConfirmees(10L)).thenReturn(0L, 2L);
        stubMapperBasique(request, karim, trajet, casa, marrakech);

        ReservationResponse response = reservationService.reserver(request);

        assertThat(response.montantTotal()).isEqualByComparingTo("160.00");
        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.COMPLET);
        verify(trajetRepository).save(trajet);
    }

    @Test
    void reserver_surTrajetComplet_doitLeverBusinessRuleException409() {
        LocalDateTime depart = LocalDateTime.now().plusDays(2);
        Passager salma = passager(1L, "Salma");
        Trajet trajet = Trajet.builder()
                .id(10L)
                .placesProposees(3)
                .dateDepart(depart)
                .statut(StatutTrajet.COMPLET)
                .build();
        PointArret settat = point(22L, trajet, "Settat", 2, depart.plusHours(2), "70.00");
        PointArret marrakech = point(23L, trajet, "Marrakech", 3, depart.plusHours(4), "120.00");

        ReservationRequest request = new ReservationRequest(1L, 10L, 22L, 23L, 1);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(salma));
        when(trajetRepository.findById(10L)).thenReturn(Optional.of(trajet));
        when(pointArretRepository.findById(22L)).thenReturn(Optional.of(settat));
        when(pointArretRepository.findById(23L)).thenReturn(Optional.of(marrakech));

        assertThatThrownBy(() -> reservationService.reserver(request))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.COMPLET);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void reserver_monteeApresDescente_doitLeverBadRequestException400() {
        LocalDateTime depart = LocalDateTime.now().plusDays(2);
        Passager salma = passager(1L, "Salma");
        Trajet trajet = trajetPublie(10L, 3, depart);
        PointArret marrakech = point(23L, trajet, "Marrakech", 3, depart.plusHours(4), "120.00");
        PointArret settat = point(22L, trajet, "Settat", 2, depart.plusHours(2), "70.00");

        ReservationRequest request = new ReservationRequest(1L, 10L, 23L, 22L, 1);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(salma));
        when(trajetRepository.findById(10L)).thenReturn(Optional.of(trajet));
        when(pointArretRepository.findById(23L)).thenReturn(Optional.of(marrakech));
        when(pointArretRepository.findById(22L)).thenReturn(Optional.of(settat));

        assertThatThrownBy(() -> reservationService.reserver(request))
                .isInstanceOf(BadRequestException.class);

        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.PUBLIE);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void annuler_aMoinsDe24Heures_doitLeverBusinessRuleException409() {
        LocalDateTime departDans2Heures = LocalDateTime.now().plusHours(2);
        Passager salma = passager(1L, "Salma");
        Trajet trajet = trajetPublie(10L, 3, departDans2Heures);
        Reservation reservation = Reservation.builder()
                .id(50L)
                .passager(salma)
                .trajet(trajet)
                .nombrePlaces(1)
                .montantTotal(new BigDecimal("50.00"))
                .statut(StatutReservation.CONFIRMEE)
                .build();

        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.annulerReservation(50L, 1L))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(reservation.getStatut()).isEqualTo(StatutReservation.CONFIRMEE);
        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.PUBLIE);
        verify(reservationRepository, never()).save(any());
    }
}
