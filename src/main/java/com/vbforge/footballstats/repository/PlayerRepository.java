package com.vbforge.footballstats.repository;

import com.vbforge.footballstats.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findByClub_Id(Long clubId);
    // Check if shirt number is taken in a specific club
    boolean existsByClubIdAndShirtNumber(Long clubId, Integer shirtNumber);

    // Check if shirt number is taken in a specific club, excluding a specific player
    boolean existsByClubIdAndShirtNumberAndIdNot(Long clubId, Integer shirtNumber, Long excludePlayerId);

    // Find all players by club (useful for squad management)
    List<Player> findByClubIdOrderByShirtNumberAsc(Long clubId);

    // Find players by position in a club
    List<Player> findByClubIdAndPosition(Long clubId, Player.Position position);

}
