package com.taxi.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.taxi.enums.StatutTrajet;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "trajets")
public class Trajet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conducteur_id", nullable = false)
    private Conducteur conducteur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    @Column(nullable = false)
    private String villeDepart;

    @Column(nullable = false)
    private String villeArrivee;

    @Column(nullable = false)
    private LocalDateTime dateDepart;

    @Column(nullable = false)
    private int placesProposees;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutTrajet statut = StatutTrajet.PUBLIE;

    @OneToMany(mappedBy = "trajet", cascade = CascadeType.ALL/* composition */, orphanRemoval = true)
    @OrderBy("ordre ASC") // composition
    @Builder.Default
    private List<PointArret> pointsArret = new ArrayList<>();

    public void addPointArret(PointArret point) {
        pointsArret.add(point);
        point.setTrajet(this);
    }

    public void removePointArret(PointArret point) {
        pointsArret.remove(point);
        point.setTrajet(null);
    }
}
