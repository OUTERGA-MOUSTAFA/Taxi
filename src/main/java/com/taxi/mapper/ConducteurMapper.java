package com.taxi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.taxi.dto.ConducteurRequest;
import com.taxi.dto.ConducteurResponse;
import com.taxi.entity.Conducteur;

@Mapper(componentModel = "spring")
public interface ConducteurMapper {
    @Mapping(target = "id", ignore = true) // bash ntfada Unmapped target property: id dyale ConducteurRequest dto bash
                                           // builld iken n9i mafihsh warning et aussi la security bash mayjish shi
                                           // conducteur isift id dyale conducteur dyale shi wahed akher, daba ignore
                                           // katmen3e
    Conducteur toEntity(ConducteurRequest request);

    // 3lash hna la: id est généré par la base et vient de l’URL lors de la mise à
    // jour : le client ne doit jamais pouvoir le fixer, donc on l’ignore
    // explicitement dans les mappings Request → Entity.
    ConducteurResponse toResponse(Conducteur conducteur);

    @Mapping(target = "id", ignore = true)
    void updateEntity(ConducteurRequest request, @MappingTarget Conducteur conducteur);
}
