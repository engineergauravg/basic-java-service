package com.app.basicjavaservice.service;

import com.app.basicjavaservice.exception.PlayerNotFoundException;
import com.app.basicjavaservice.exception.PlayerServiceException;
import com.app.basicjavaservice.mapper.PlayerMapper;
import com.app.basicjavaservice.model.CreatePlayerRequest;
import com.app.basicjavaservice.model.PaginationMetadata;
import com.app.basicjavaservice.model.PaginationRequest;
import com.app.basicjavaservice.model.Player;
import com.app.basicjavaservice.model.PlayerResponse;
import com.app.basicjavaservice.model.PlayersResponse;
import com.app.basicjavaservice.model.UpdatePlayerRequest;
import com.app.basicjavaservice.repository.PlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerMapper playerMapper;

    public PlayersResponse getPlayers(PaginationRequest request) {
        Pageable pageable = PageRequest.of(
                request.isCursorBased() ? 0 : (request.page() != null ? request.page() : 0),
                request.size(),
                Sort.by(Sort.Direction.ASC, "playerId")
        );

        Page<Player> page = request.isCursorBased()
                ? playerRepository.findByPlayerIdGreaterThan(request.after(), pageable)
                : playerRepository.findAll(pageable);

        List<PlayerResponse> players = page.getContent().stream()
                .map(playerMapper::toResponse)
                .toList();

        String nextCursor = null;
        Integer nextPage = null;

        if (page.hasNext()) {
            if (request.isCursorBased()) {
                nextCursor = players.isEmpty() ? null : players.get(players.size() - 1).playerId();
            } else {
                nextPage = page.getNumber() + 1;
            }
        }

        PaginationMetadata metadata = new PaginationMetadata(
                request.size(),
                page.hasNext(),
                nextCursor,
                nextPage
        );

        return new PlayersResponse(players, metadata);
    }

    @Cacheable(value = "players", key = "#playerId")
    public Player getPlayerById(String playerId) {
        try {
            /* simulated network delay */
            Thread.sleep((long) (Math.random() * 2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    public PlayerResponse createPlayer(CreatePlayerRequest request) {
        try {
            Player saved = playerRepository.save(playerMapper.toEntity(request));
            return playerMapper.toResponse(saved);
        } catch (Exception e) {
            log.error("message=Exception in createPlayer; exception={}", e.toString());
            throw new PlayerServiceException("Failed to create player", e);
        }
    }

    @CacheEvict(value = "players", key = "#playerId")
    public void deletePlayer(String playerId) {
        if (!playerRepository.existsById(playerId)) {
            throw new PlayerNotFoundException(playerId);
        }
        playerRepository.deleteById(playerId);
    }

    @CacheEvict(value = "players", key = "#playerId")
    public PlayerResponse updatePlayer(String playerId, UpdatePlayerRequest request) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
        try {
            playerMapper.updateEntity(request, player);
            return playerMapper.toResponse(playerRepository.save(player));
        } catch (Exception e) {
            log.error("message=Exception in updatePlayer; playerId={}; exception={}", playerId, e.toString());
            throw new PlayerServiceException("Failed to update player", e);
        }
    }
}
