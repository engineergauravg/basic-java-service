package com.app.playerservicejava.controller;

import com.app.playerservicejava.config.PlayerProperties;
import com.app.playerservicejava.exception.ErrorCodeHttpStatusMapper;
import com.app.playerservicejava.exception.PlayerNotFoundException;
import com.app.playerservicejava.exception.PlayerServiceException;
import com.app.playerservicejava.model.CreatePlayerRequest;
import com.app.playerservicejava.model.PaginationMetadata;
import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.model.PlayerResponse;
import com.app.playerservicejava.model.PlayersResponse;
import com.app.playerservicejava.model.UpdatePlayerRequest;
import com.app.playerservicejava.service.PlayerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlayerController.class)
@Import(ErrorCodeHttpStatusMapper.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private PlayerProperties playerProperties;

    private PlayerResponse buildPlayerResponse(String id) {
        return new PlayerResponse(id, "John", "Doe", null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);
    }

    private PlayersResponse buildPlayersResponse(boolean hasMore) {
        List<PlayerResponse> players = List.of(buildPlayerResponse("p1"));
        PaginationMetadata meta = new PaginationMetadata(10, hasMore, null, hasMore ? 1 : null);
        return new PlayersResponse(players, meta);
    }

    // ── GET /v1/players ──────────────────────────────────────────────────────

    @Test
    void getPlayers_withSizeParam_returnsOk() throws Exception {
        when(playerService.getPlayers(any())).thenReturn(buildPlayersResponse(false));

        mockMvc.perform(get("/v1/players").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players[0].playerId").value("p1"))
                .andExpect(jsonPath("$.pagination.hasMore").value(false));
    }

    @Test
    void getPlayers_sizeZeroUsesPropertyDefault_returnsOk() throws Exception {
        when(playerProperties.getPageLimit()).thenReturn(20);
        when(playerService.getPlayers(any())).thenReturn(buildPlayersResponse(true));

        mockMvc.perform(get("/v1/players").param("size", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.hasMore").value(true));
    }

    @Test
    void getPlayers_withPageParam_returnsOk() throws Exception {
        when(playerService.getPlayers(any())).thenReturn(buildPlayersResponse(false));

        mockMvc.perform(get("/v1/players").param("size", "10").param("page", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void getPlayers_withAfterCursor_returnsOk() throws Exception {
        when(playerService.getPlayers(any())).thenReturn(buildPlayersResponse(false));

        mockMvc.perform(get("/v1/players").param("size", "10").param("after", "cursor-abc"))
                .andExpect(status().isOk());
    }

    @Test
    void getPlayers_missingSizeParam_returns500() throws Exception {
        // MissingServletRequestParameterException is caught by the generic Exception handler
        // in GlobalExceptionHandler (which doesn't extend ResponseEntityExceptionHandler),
        // so it returns 500 instead of 400.
        mockMvc.perform(get("/v1/players"))
                .andExpect(status().isInternalServerError());
    }

    // ── GET /v1/players/{id} ─────────────────────────────────────────────────

    @Test
    void getPlayerById_found_returnsOk() throws Exception {
        Player player = new Player();
        player.setPlayerId("p1");
        player.setFirstName("John");
        player.setLastName("Doe");

        when(playerService.getPlayerById("p1")).thenReturn(player);

        mockMvc.perform(get("/v1/players/p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value("p1"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void getPlayerById_notFound_returns404() throws Exception {
        when(playerService.getPlayerById("missing")).thenThrow(new PlayerNotFoundException("missing"));

        mockMvc.perform(get("/v1/players/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PLAYER_NOT_FOUND"));
    }

    // ── POST /v1/players ─────────────────────────────────────────────────────

    @Test
    void createPlayer_validRequest_returnsCreated() throws Exception {
        CreatePlayerRequest req = new CreatePlayerRequest(
                "John", "Doe", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        PlayerResponse response = buildPlayerResponse("p1");

        when(playerService.createPlayer(any(CreatePlayerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.playerId").value("p1"));
    }

    @Test
    void createPlayer_serviceThrows_returns500() throws Exception {
        CreatePlayerRequest req = new CreatePlayerRequest(
                "John", "Doe", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        when(playerService.createPlayer(any(CreatePlayerRequest.class)))
                .thenThrow(new PlayerServiceException("Failed to create player", new RuntimeException()));

        mockMvc.perform(post("/v1/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("PLAYER_SERVICE_ERROR"));
    }

    // ── PUT /v1/players/{id} ─────────────────────────────────────────────────

    @Test
    void updatePlayer_found_returnsOk() throws Exception {
        UpdatePlayerRequest req = new UpdatePlayerRequest(
                "Jane", "Doe", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        PlayerResponse response = buildPlayerResponse("p1");

        when(playerService.updatePlayer(eq("p1"), any(UpdatePlayerRequest.class))).thenReturn(response);

        mockMvc.perform(put("/v1/players/p1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value("p1"));
    }

    @Test
    void updatePlayer_notFound_returns404() throws Exception {
        UpdatePlayerRequest req = new UpdatePlayerRequest(
                "Jane", "Doe", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        when(playerService.updatePlayer(eq("missing"), any(UpdatePlayerRequest.class)))
                .thenThrow(new PlayerNotFoundException("missing"));

        mockMvc.perform(put("/v1/players/missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PLAYER_NOT_FOUND"));
    }
}
