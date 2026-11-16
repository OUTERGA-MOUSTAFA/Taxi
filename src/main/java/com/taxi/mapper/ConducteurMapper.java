package com.taxi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.taxi.dto.ConducteurRequest;
import com.taxi.dto.ConducteurResponse;
import com.taxi.entity.Conducteur;

@Mapper (componentModel = "spring")
public interface ConducteurMapper {
    Conducteur toEntity(ConducteurRequest request);
    ConducteurResponse toResponse(Conducteur conducteur);
    void updateEntity(ConducteurRequest request, @MappingTarget Conducteur conducteur);
}
