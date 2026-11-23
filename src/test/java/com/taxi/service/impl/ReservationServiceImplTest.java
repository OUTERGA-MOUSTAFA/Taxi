package com.taxi.service.impl;
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
                    r.getStatut()
            );
        });
    }
}
