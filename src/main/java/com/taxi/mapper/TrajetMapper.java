package com.taxi.mapper;

import com.taxi.dto.PointArretRequest;
import com.taxi.dto.PointArretResponse;
import com.taxi.dto.TrajetRequest;
import com.taxi.dto.TrajetResponse;
import com.taxi.entity.PointArret;
import com.taxi.entity.Trajet;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TrajetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conducteur", ignore = true)
    @Mapping(target = "vehicule", ignore = true)
    @Mapping(target = "villeDepart", ignore = true)
    @Mapping(target = "villeArrivee", ignore = true)
    @Mapping(target = "dateDepart", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "pointsArret", ignore = true)
    Trajet toEntity(TrajetRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trajet", ignore = true)
    @Mapping(target = "ordre", ignore = true)
    PointArret toPointEntity(PointArretRequest request);

    PointArretResponse toPointResponse(PointArret pointArret);

    @Mapping(source = "conducteur.id", target = "conducteurId")
    @Mapping(source = "conducteur.nom", target = "conducteurNom")
    @Mapping(source = "vehicule.id", target = "vehiculeId")
    @Mapping(source = "vehicule.immatriculation", target = "vehiculeImmatriculation")
    @Mapping(target = "placesRestantes", ignore = true)// hna kangole l mapStruct ha hewa mat mshish t9elbi 3lih f class Trajet
    TrajetResponse toResponse(Trajet trajet);
}