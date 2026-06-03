package com.app.playerservicejava.mapper;

import com.app.playerservicejava.model.CreatePlayerRequest;
import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.model.PlayerResponse;
import com.app.playerservicejava.model.UpdatePlayerRequest;
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
