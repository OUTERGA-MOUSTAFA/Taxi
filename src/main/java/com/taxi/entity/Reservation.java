package com.taxi.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.taxi.enums.StatutReservation;

import jakarta.persistence.*;
import lombok.*;

@Entity 
@Table(name = "reservations")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
@Builder 
public class Reservation {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "passager_id", nullable = false)
    private Passager passager;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trajet_id", nullable = false)
    private Trajet trajet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "point_montee_id", nullable = false)
    private PointArret pointMontee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "point_descente_id", nullable = false)
    private PointArret pointDescente;

    @Column(nullable = false)
    private int nombrePlaces;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTotal;

    @Column(nullable = false)
    private LocalDateTime dateReservation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutReservation statut = StatutReservation.EN_ATTENTE;

    @PrePersist
    protected void onCreate() {
        if (this.dateReservation == null) {
            this.dateReservation = LocalDateTime.now();
        }
        if (this.statut == null) {
            this.statut = StatutReservation.EN_ATTENTE;
        }
    }
}
