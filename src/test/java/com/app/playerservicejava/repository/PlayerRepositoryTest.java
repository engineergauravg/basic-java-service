package com.app.playerservicejava.repository;

import com.app.playerservicejava.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PlayerRepositoryTest {

    @Autowired
    private PlayerRepository playerRepository;

    private Player player1;
    private Player player2;
    private Player player3;
    // IDs sorted lexicographically — mirrors the ASC playerId sort order used by the service
    private List<String> sortedIds;

    @BeforeEach
    void setUp() {
        playerRepository.deleteAll();

        // IDs are auto-generated (UUID) — capture after save
        player1 = new Player();
        player1.setFirstName("Alice");
        player1.setLastName("Smith");
        player1 = playerRepository.save(player1);

        player2 = new Player();
        player2.setFirstName("Bob");
        player2.setLastName("Smith");
        player2 = playerRepository.save(player2);

        player3 = new Player();
        player3.setFirstName("Charlie");
        player3.setLastName("Jones");
        player3 = playerRepository.save(player3);

        sortedIds = Stream.of(player1, player2, player3)
                .map(Player::getPlayerId)
                .sorted()
                .toList();
    }

    // ── findAll (offset pagination) ──────────────────────────────────────────

    @Test
    void findAll_firstPage_returnsPageWithHasNext() {
        Page<Player> page = playerRepository.findAll(PageRequest.of(0, 2, Sort.by("playerId")));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void findAll_secondPage_returnsRemainingAndHasNoNext() {
        Page<Player> page = playerRepository.findAll(PageRequest.of(1, 2, Sort.by("playerId")));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.hasNext()).isFalse();
        assertThat(page.getContent().get(0).getPlayerId()).isEqualTo(sortedIds.get(2));
    }

    @Test
    void findAll_sortedByPlayerId_returnsInAscendingOrder() {
        Page<Player> page = playerRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "playerId")));

        List<String> ids = page.getContent().stream().map(Player::getPlayerId).toList();
        assertThat(ids).containsExactlyElementsOf(sortedIds);
    }

    // ── findByPlayerIdGreaterThan (cursor pagination) ────────────────────────

    @Test
    void findByPlayerIdGreaterThan_afterFirstCursor_returnsRemaining() {
        Page<Player> page = playerRepository.findByPlayerIdGreaterThan(
                sortedIds.get(0), PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "playerId")));

        assertThat(page.getContent()).extracting(Player::getPlayerId)
                .containsExactly(sortedIds.get(1), sortedIds.get(2));
    }

    @Test
    void findByPlayerIdGreaterThan_afterLastCursor_returnsEmpty() {
        Page<Player> page = playerRepository.findByPlayerIdGreaterThan(
                sortedIds.get(2), PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "playerId")));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    void findByPlayerIdGreaterThan_withPageSize_respectsPageLimit() {
        Page<Player> page = playerRepository.findByPlayerIdGreaterThan(
                sortedIds.get(0), PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "playerId")));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getPlayerId()).isEqualTo(sortedIds.get(1));
        assertThat(page.hasNext()).isTrue();
    }

    // ── findByLastNameIgnoreCase ──────────────────────────────────────────────

    @Test
    void findByLastNameIgnoreCase_exactMatch_returnsPlayers() {
        List<Player> result = playerRepository.findByLastNameIgnoreCase("Smith");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Player::getLastName).containsOnly("Smith");
    }

    @Test
    void findByLastNameIgnoreCase_lowercaseInput_returnsPlayers() {
        List<Player> result = playerRepository.findByLastNameIgnoreCase("smith");

        assertThat(result).hasSize(2);
    }

    @Test
    void findByLastNameIgnoreCase_uppercaseInput_returnsPlayers() {
        List<Player> result = playerRepository.findByLastNameIgnoreCase("JONES");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Charlie");
    }

    @Test
    void findByLastNameIgnoreCase_noMatch_returnsEmpty() {
        List<Player> result = playerRepository.findByLastNameIgnoreCase("Unknown");

        assertThat(result).isEmpty();
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_existingId_returnsPlayer() {
        Optional<Player> result = playerRepository.findById(player1.getPlayerId());

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("Alice");
    }

    @Test
    void findById_nonExistingId_returnsEmpty() {
        Optional<Player> result = playerRepository.findById("nonexistent");

        assertThat(result).isEmpty();
    }
}
