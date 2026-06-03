package com.app.playerservicejava.repository;

import com.app.playerservicejava.model.Player;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, String> {

    // offset pagination
    Page<Player> findAll(Pageable pageable);

    // cursor pagination
    Page<Player> findByPlayerIdGreaterThan(String playerId, Pageable pageable);

    // name search for intent resolution
    List<Player> findByLastNameIgnoreCase(String lastName);
}
