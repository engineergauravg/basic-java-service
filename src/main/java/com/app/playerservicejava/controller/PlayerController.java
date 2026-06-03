package com.app.playerservicejava.controller;

import com.app.playerservicejava.config.PlayerProperties;
import com.app.playerservicejava.model.CreatePlayerRequest;
import com.app.playerservicejava.model.PaginationRequest;
import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.model.PlayerResponse;
import com.app.playerservicejava.model.PlayersResponse;
import com.app.playerservicejava.model.UpdatePlayerRequest;
import com.app.playerservicejava.service.PlayerService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "v1/players", produces = {MediaType.APPLICATION_JSON_VALUE})
public class PlayerController {

    @Resource
    private PlayerService playerService;

    @Autowired
    private PlayerProperties playerProperties;


    @GetMapping
    public ResponseEntity<PlayersResponse> getPlayers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) String after,
            @RequestParam() int size) {
        size = size == 0 ? playerProperties.getPageLimit() : size;
        PaginationRequest request = new PaginationRequest(page, after, size);
        return ResponseEntity.ok(playerService.getPlayers(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable("id") String id) {
        return ResponseEntity.ok(playerService.getPlayerById(id));
    }

    @PostMapping
    public ResponseEntity<PlayerResponse> createPlayer(@RequestBody CreatePlayerRequest request) {
        return new ResponseEntity<>(playerService.createPlayer(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerResponse> updatePlayer(@PathVariable("id") String id,
                                                       @RequestBody UpdatePlayerRequest request) {
        return ResponseEntity.ok(playerService.updatePlayer(id, request));
    }
}
