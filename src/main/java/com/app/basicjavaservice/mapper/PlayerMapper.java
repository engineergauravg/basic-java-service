package com.app.basicjavaservice.mapper;

import com.app.basicjavaservice.model.CreatePlayerRequest;
import com.app.basicjavaservice.model.Player;
import com.app.basicjavaservice.model.PlayerResponse;
import com.app.basicjavaservice.model.UpdatePlayerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    @Mapping(target = "playerId", ignore = true)
    Player toEntity(CreatePlayerRequest request);

    @Mapping(target = "playerId", ignore = true)
    void updateEntity(UpdatePlayerRequest request, @MappingTarget Player player);

    PlayerResponse toResponse(Player player);
}
