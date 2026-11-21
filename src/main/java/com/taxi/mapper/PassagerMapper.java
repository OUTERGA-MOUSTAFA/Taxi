package com.taxi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.taxi.dto.PassagerRequest;
import com.taxi.dto.PassagerResponse;
import com.taxi.entity.Passager;

@Mapper (componentModel = "spring")
public interface PassagerMapper {
    @Mapping(target = "id", ignore = true)// bash ntfada Unmapped target property: id dyale ConducteurRequest dto
    Passager toEntity(PassagerRequest request);
    PassagerResponse toResponse(Passager passager);
    @Mapping (target = "id", ignore = true)
    void updateEntity(PassagerRequest request, @MappingTarget Passager passager);
}
