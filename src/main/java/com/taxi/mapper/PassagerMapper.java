package com.taxi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.taxi.dto.PassagerRequest;
import com.taxi.dto.PassagerResponse;
import com.taxi.entity.Passager;

@Mapper (componentModel = "spring")
public interface PassagerMapper {
    Passager toEntity(PassagerRequest request);
    PassagerResponse toResponse(Passager passager);
    void updateEntity(PassagerRequest request, @MappingTarget Passager passager);
}
