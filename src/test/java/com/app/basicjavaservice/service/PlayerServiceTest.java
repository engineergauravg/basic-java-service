package com.app.basicjavaservice.service;

import com.app.basicjavaservice.exception.PlayerNotFoundException;
import com.app.basicjavaservice.exception.PlayerServiceException;
import com.app.basicjavaservice.mapper.PlayerMapper;
import com.app.basicjavaservice.model.CreatePlayerRequest;
import com.app.basicjavaservice.model.PaginationRequest;
import com.app.basicjavaservice.model.Player;
import com.app.basicjavaservice.model.PlayerResponse;
import com.app.basicjavaservice.model.PlayersResponse;
import com.app.basicjavaservice.model.UpdatePlayerRequest;
import com.app.basicjavaservice.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerMapper playerMapper;

    @InjectMocks
    private PlayerService playerService;

    private Player buildPlayer(String id) {
        Player p = new Player();
        p.setPlayerId(id);
        p.setFirstName("John");
        p.setLastName("Doe");
        return p;
    }

    private PlayerResponse buildPlayerResponse(String id) {
        return new PlayerResponse(id, "John", "Doe", null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);
    }

    // ── getPlayers ──────────────────────────────────────────────────────────

    @Test
    void getPlayers_offsetPagination_returnsPlayersResponse() {
        PaginationRequest request = new PaginationRequest(0, null, 10);
        Player player = buildPlayer("p1");
        PlayerResponse response = buildPlayerResponse("p1");
        Page<Player> page = new PageImpl<>(List.of(player), PageRequest.of(0, 10), 20);

        when(playerRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(playerMapper.toResponse(player)).thenReturn(response);

        PlayersResponse result = playerService.getPlayers(request);

        assertThat(result.players()).hasSize(1);
        assertThat(result.pagination().hasMore()).isTrue();
        assertThat(result.pagination().nextPage()).isEqualTo(1);
        assertThat(result.pagination().nextCursor()).isNull();
    }

    @Test
    void getPlayers_cursorPagination_returnsNextCursor() {
        PaginationRequest request = new PaginationRequest(null, "cursor-id", 10);
        Player player = buildPlayer("p2");
        PlayerResponse response = buildPlayerResponse("p2");
        Page<Player> page = new PageImpl<>(List.of(player), PageRequest.of(0, 10), 20);

        when(playerRepository.findByPlayerIdGreaterThan(eq("cursor-id"), any(Pageable.class))).thenReturn(page);
        when(playerMapper.toResponse(player)).thenReturn(response);

        PlayersResponse result = playerService.getPlayers(request);

        assertThat(result.players()).hasSize(1);
        assertThat(result.pagination().hasMore()).isTrue();
        assertThat(result.pagination().nextCursor()).isEqualTo("p2");
        assertThat(result.pagination().nextPage()).isNull();
    }

    @Test
    void getPlayers_lastPage_hasMoreFalseAndNoNextCursor() {
        PaginationRequest request = new PaginationRequest(0, null, 10);
        Player player = buildPlayer("p1");
        PlayerResponse response = buildPlayerResponse("p1");
        Page<Player> page = new PageImpl<>(List.of(player), PageRequest.of(0, 10), 1);

        when(playerRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(playerMapper.toResponse(player)).thenReturn(response);

        PlayersResponse result = playerService.getPlayers(request);

        assertThat(result.pagination().hasMore()).isFalse();
        assertThat(result.pagination().nextPage()).isNull();
        assertThat(result.pagination().nextCursor()).isNull();
    }

    @Test
    void getPlayers_emptyResult_returnsEmptyList() {
        PaginationRequest request = new PaginationRequest(0, null, 10);
        Page<Player> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(playerRepository.findAll(any(Pageable.class))).thenReturn(page);

        PlayersResponse result = playerService.getPlayers(request);

        assertThat(result.players()).isEmpty();
        assertThat(result.pagination().hasMore()).isFalse();
    }

    // ── getPlayerById ────────────────────────────────────────────────────────

    @Test
    void getPlayerById_found_returnsPlayer() {
        Player player = buildPlayer("p1");
        when(playerRepository.findById("p1")).thenReturn(Optional.of(player));

        Player result = playerService.getPlayerById("p1");

        assertThat(result).isEqualTo(player);
    }

    @Test
    void getPlayerById_notFound_throwsPlayerNotFoundException() {
        when(playerRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.getPlayerById("missing"))
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessageContaining("missing");
    }

    // ── createPlayer ─────────────────────────────────────────────────────────

    @Test
    void createPlayer_success_returnsPlayerResponse() {
        CreatePlayerRequest req = new CreatePlayerRequest(
                "John", "Doe", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        Player entity = buildPlayer("p1");
        PlayerResponse response = buildPlayerResponse("p1");

        when(playerMapper.toEntity(req)).thenReturn(entity);
        when(playerRepository.save(entity)).thenReturn(entity);
        when(playerMapper.toResponse(entity)).thenReturn(response);

        PlayerResponse result = playerService.createPlayer(req);

        assertThat(result.playerId()).isEqualTo("p1");
        verify(playerRepository).save(entity);
    }

    @Test
    void createPlayer_repositoryThrows_throwsPlayerServiceException() {
        CreatePlayerRequest req = new CreatePlayerRequest(
                "John", "Doe", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        Player entity = buildPlayer(null);

        when(playerMapper.toEntity(req)).thenReturn(entity);
        when(playerRepository.save(entity)).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> playerService.createPlayer(req))
                .isInstanceOf(PlayerServiceException.class)
                .hasMessageContaining("Failed to create player");
    }

    // ── updatePlayer ─────────────────────────────────────────────────────────

    @Test
    void updatePlayer_success_returnsPlayerResponse() {
        UpdatePlayerRequest req = new UpdatePlayerRequest(
                "Jane", "Doe", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        Player player = buildPlayer("p1");
        PlayerResponse response = buildPlayerResponse("p1");

        when(playerRepository.findById("p1")).thenReturn(Optional.of(player));
        when(playerRepository.save(player)).thenReturn(player);
        when(playerMapper.toResponse(player)).thenReturn(response);

        PlayerResponse result = playerService.updatePlayer("p1", req);

        assertThat(result.playerId()).isEqualTo("p1");
        verify(playerMapper).updateEntity(req, player);
    }

    @Test
    void updatePlayer_notFound_throwsPlayerNotFoundException() {
        UpdatePlayerRequest req = new UpdatePlayerRequest(
                "Jane", "Doe", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        when(playerRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.updatePlayer("missing", req))
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessageContaining("missing");
    }

    // ── deletePlayer ─────────────────────────────────────────────────────────

    @Test
    void deletePlayer_existing_deletesPlayer() {
        when(playerRepository.existsById("p1")).thenReturn(true);

        playerService.deletePlayer("p1");

        verify(playerRepository).deleteById("p1");
    }

    @Test
    void deletePlayer_notFound_throwsAndDoesNotDelete() {
        when(playerRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> playerService.deletePlayer("missing"))
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessageContaining("missing");

        verify(playerRepository, never()).deleteById("missing");
    }

    @Test
    void updatePlayer_repositoryThrows_throwsPlayerServiceException() {
        UpdatePlayerRequest req = new UpdatePlayerRequest(
                "Jane", "Doe", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        Player player = buildPlayer("p1");

        when(playerRepository.findById("p1")).thenReturn(Optional.of(player));
        when(playerRepository.save(player)).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> playerService.updatePlayer("p1", req))
                .isInstanceOf(PlayerServiceException.class)
                .hasMessageContaining("Failed to update player");
    }
}
