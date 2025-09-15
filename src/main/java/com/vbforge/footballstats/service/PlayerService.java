package com.vbforge.footballstats.service;

import com.vbforge.footballstats.dto.action.PlayerStatisticsDTO;
import com.vbforge.footballstats.dto.player.PlayerDTO;
import com.vbforge.footballstats.entity.Player;

import java.util.List;

public interface PlayerService {

    // Basic CRUD operations
    List<Player> getAllPlayers();
    List<Player> getPlayersByClub(Long clubId);

    void deletePlayer(Long playerId);



    PlayerStatisticsDTO getPlayerDetail(Long playerId);
    List<PlayerStatisticsDTO> getAllPlayerStatistics();


    /**
     * Create a new player from DTO
     */
    Player createPlayer(PlayerDTO playerDTO);

    /**
     * Update existing player from DTO
     */
    Player updatePlayer(PlayerDTO playerDTO);

    /**
     * Get player by ID
     */
    Player getPlayerById(Long id);

    /**
     * Check if shirt number is taken in a club
     */
    boolean isShirtNumberTaken(Long clubId, Integer shirtNumber);

    /**
     * Check if shirt number is taken in a club, excluding a specific player (for updates)
     */
    boolean isShirtNumberTakenExcluding(Long clubId, Integer shirtNumber, Long excludePlayerId);

    /**
     * Save player (legacy method - use createPlayer/updatePlayer instead)
     */
    void savePlayer(Player player);



}
