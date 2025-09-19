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
    Player createPlayer(PlayerDTO playerDTO);
    Player updatePlayer(PlayerDTO playerDTO);
    Player getPlayerById(Long id);
    boolean isShirtNumberTaken(Long clubId, Integer shirtNumber);
    boolean isShirtNumberTakenExcluding(Long clubId, Integer shirtNumber, Long excludePlayerId);

    //legacy method
    void savePlayer(Player player);



}
