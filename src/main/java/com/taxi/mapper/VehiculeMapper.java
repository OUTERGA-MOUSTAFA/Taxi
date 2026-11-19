package com.taxi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.taxi.dto.VehiculeRequest;
import com.taxi.dto.VehiculeResponse;
import com.taxi.entity.Vehicule;

@Mapper (componentModel = "spring")
public interface VehiculeMapper {

    @Mapping (target = "id", ignore = true)
    @Mapping(target = "conducteur", ignore = true)
    Vehicule toEntity(VehiculeRequest request);

    @Mapping(source = "conducteur.id", target = "conducteurId")
    @Mapping(source = "conducteur.nom", target = "conducteurNom")
    VehiculeResponse toResponse(Vehicule vehicule);//  return new VehiculeResponse( id, marque, modele, immatriculation, couleur, capacite, conducteurId, conducteurNom );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conducteur", ignore = true)
    void updateEntity(VehiculeRequest request, @MappingTarget Vehicule vehicule);
}