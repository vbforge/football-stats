package com.vbforge.footballstats.service;

import com.vbforge.footballstats.dto.PlayerStatisticsDTO;
import com.vbforge.footballstats.dto.PlayerStatsDTO;
import com.vbforge.footballstats.dto.StreakResultDTO;
import com.vbforge.footballstats.entity.Player;

import java.util.List;

public interface PlayerService {

    List<Player> getAllPlayers();
    List<Player> getPlayersByClub(Long clubId);
    List<PlayerStatsDTO> getClubTopScorers(Long clubId, int limit);
    List<PlayerStatsDTO> getClubTopAssisters(Long clubId, int limit);
    PlayerStatisticsDTO getPlayerDetail(Long playerId);
    Player getPlayerById(Long playerId);
    void updatePlayer(Player player);
    void deletePlayer(Long playerId);
    void savePlayer(Player player);
    List<PlayerStatisticsDTO> getAllPlayerStatistics();
    List<PlayerStatisticsDTO> getPlayerStatisticsByClub(Long clubId);
    StreakResultDTO calculatePlayerStreaks(Long playerId);

}
