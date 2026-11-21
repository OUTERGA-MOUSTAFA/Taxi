package com.taxi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.taxi.dto.ReservationRequest;
import com.taxi.dto.ReservationResponse;
import com.taxi.entity.Reservation;

@Mapper (componentModel = "spring")
public interface ReservationMapper {

    @Mapping (target = "id", ignore = true)
    @Mapping(target = "passager", ignore = true)
    @Mapping(target = "trajet", ignore = true)
    @Mapping(target = "pointMontee", ignore = true)
    @Mapping(target = "pointDescente", ignore = true)
    @Mapping(target = "montantTotal", ignore = true)
    @Mapping(target = "dateReservation", ignore = true)
    @Mapping(target = "statut", ignore = true)
    Reservation toEntity(ReservationRequest request);

    @Mapping(source = "passager.id", target = "passagerId")
    @Mapping(source = "passager.nom", target = "passagerNom")
    @Mapping(source = "trajet.id", target = "trajetId")
    @Mapping(source = "pointMontee.id", target = "pointMonteeId")
    @Mapping(source = "pointMontee.ville", target = "villeMontee")
    @Mapping(source = "pointDescente.id", target = "pointDescenteId")
    @Mapping(source = "pointDescente.ville", target = "villeDescente")
    ReservationResponse toResponse(Reservation reservation);
}