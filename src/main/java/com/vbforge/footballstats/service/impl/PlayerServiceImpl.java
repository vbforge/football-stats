package com.vbforge.footballstats.service.impl;

import com.vbforge.footballstats.dto.PlayerStatisticsDTO;
import com.vbforge.footballstats.dto.PlayerStatsDTO;
import com.vbforge.footballstats.dto.StreakResultDTO;
import com.vbforge.footballstats.entity.Action;
import com.vbforge.footballstats.entity.Player;
import com.vbforge.footballstats.repository.ActionRepository;
import com.vbforge.footballstats.repository.PlayerRepository;
import com.vbforge.footballstats.service.PlayerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final ActionRepository actionRepository;

    public PlayerServiceImpl(PlayerRepository playerRepository, ActionRepository actionRepository) {
        this.playerRepository = playerRepository;
        this.actionRepository = actionRepository;
    }

    @Override
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @Override
    public List<Player> getPlayersByClub(Long clubId) {
        return playerRepository.findByClub_Id(clubId);
    }

    @Override
    public List<PlayerStatsDTO> getClubTopScorers(Long clubId, int limit) {
        List<PlayerStatisticsDTO> playerStats = getPlayerStatisticsByClub(clubId);

        return playerStats.stream()
                .filter(p -> p.getTotalGoals() != null && p.getTotalGoals() > 0)
                .sorted((p1, p2) -> Integer.compare(p2.getTotalGoals(), p1.getTotalGoals()))
                .limit(limit)
                .map(PlayerStatsDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlayerStatsDTO> getClubTopAssisters(Long clubId, int limit) {
        List<PlayerStatisticsDTO> playerStats = getPlayerStatisticsByClub(clubId);

        return playerStats.stream()
                .filter(p -> p.getTotalAssists() != null && p.getTotalAssists() > 0)
                .sorted((p1, p2) -> Integer.compare(p2.getTotalAssists(), p1.getTotalAssists()))
                .limit(limit)
                .map(PlayerStatsDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public PlayerStatisticsDTO getPlayerDetail(Long playerId) {
        List<PlayerStatisticsDTO> allPlayers = getAllPlayerStatistics();
        return allPlayers.stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Player not found with id: " + playerId));
    }

    @Override
    public Player getPlayerById(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found with id: " + playerId));
    }

    @Override
    public void updatePlayer(Player player) {
        if (playerRepository.existsById(player.getId())) {
            playerRepository.save(player);
        } else {
            throw new EntityNotFoundException("Player not found with id: " + player.getId());
        }
    }

    @Override
    public void deletePlayer(Long playerId) {
        if (playerRepository.existsById(playerId)) {
            // First delete all actions related to this player
            actionRepository.deleteByPlayerId(playerId);
            // Then delete the player
            playerRepository.deleteById(playerId);
        } else {
            throw new EntityNotFoundException("Player not found with id: " + playerId);
        }
    }

    @Override
    public void savePlayer(Player player) {
        playerRepository.save(player);
    }

    @Override
    public List<PlayerStatisticsDTO> getAllPlayerStatistics() {
        List<Object[]> results = actionRepository.getPlayerStatistics();
        return results.stream()
                .map(result -> {
                    PlayerStatisticsDTO dto = new PlayerStatisticsDTO(
                            (Long) result[0],      // playerId
                            (String) result[1],    // playerName
                            (String) result[2],    // clubName
                            (Long) result[3],      // totalGoals
                            (Long) result[4],      // totalAssists
                            (Long) result[5]       // totalPoints
                    );
                    // Calculate streaks
                    StreakResultDTO streaks = calculatePlayerStreaks(dto.getPlayerId());
                    dto.setMaxGoalStreak(streaks.getMaxGoalStreak());
                    dto.setMaxAssistStreak(streaks.getMaxAssistStreak());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PlayerStatisticsDTO> getPlayerStatisticsByClub(Long clubId) {
        List<Object[]> results = actionRepository.getPlayerStatisticsByClub(clubId);
        return results.stream()
                .map(result -> {
                    PlayerStatisticsDTO dto = new PlayerStatisticsDTO(
                            (Long) result[0],      // playerId
                            (String) result[1],    // playerName
                            (String) result[2],    // clubName
                            (Long) result[3],      // totalGoals
                            (Long) result[4],      // totalAssists
                            (Long) result[5]       // totalPoints
                    );
                    // Calculate streaks
                    StreakResultDTO streaks = calculatePlayerStreaks(dto.getPlayerId());
                    dto.setMaxGoalStreak(streaks.getMaxGoalStreak());
                    dto.setMaxAssistStreak(streaks.getMaxAssistStreak());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public StreakResultDTO calculatePlayerStreaks(Long playerId) {
        List<Action> actions = actionRepository.findByPlayerIdOrderByMatchDay(playerId);

        int maxGoalStreak = 0;
        int maxAssistStreak = 0;
        int currentGoalStreak = 0;
        int currentAssistStreak = 0;

        for (Action action : actions) {
            // Goal streak calculation
            if (action.getGoals() > 0) {
                currentGoalStreak++;
                maxGoalStreak = Math.max(maxGoalStreak, currentGoalStreak);
            } else {
                currentGoalStreak = 0;
            }

            // Assist streak calculation
            if (action.getAssists() > 0) {
                currentAssistStreak++;
                maxAssistStreak = Math.max(maxAssistStreak, currentAssistStreak);
            } else {
                currentAssistStreak = 0;
            }
        }

        return new StreakResultDTO(maxGoalStreak, maxAssistStreak,
                currentGoalStreak, currentAssistStreak);
    }

}
