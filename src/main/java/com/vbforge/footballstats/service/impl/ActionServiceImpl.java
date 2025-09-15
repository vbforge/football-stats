package com.vbforge.footballstats.service.impl;

import com.vbforge.footballstats.dto.action.*;
import com.vbforge.footballstats.entity.*;
import com.vbforge.footballstats.repository.*;
import com.vbforge.footballstats.service.ActionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActionServiceImpl implements ActionService {

    private final SeasonRepository seasonRepository;
    private final MatchDayRepository matchDayRepository;
    private final ClubRepository clubRepository;
    private final PlayerRepository playerRepository;
    private final ActionRepository actionRepository;

    public ActionServiceImpl(SeasonRepository seasonRepository,
                             MatchDayRepository matchDayRepository,
                             ClubRepository clubRepository,
                             PlayerRepository playerRepository,
                             ActionRepository actionRepository) {
        this.seasonRepository = seasonRepository;
        this.matchDayRepository = matchDayRepository;
        this.clubRepository = clubRepository;
        this.playerRepository = playerRepository;
        this.actionRepository = actionRepository;
    }

    @Override
    @Transactional
    public void saveAction(ActionFormDTO actionForm) {
        // Get current season
        Season currentSeason = seasonRepository.findByIsCurrentTrue()
                .orElseThrow(() -> new IllegalStateException("No current season is set"));

        // Get or create match day for current season
        MatchDay matchDay = matchDayRepository.findByNumberAndSeason(actionForm.getMatchDayNumber(), currentSeason)
                .orElseGet(() -> {
                    MatchDay newMatchDay = new MatchDay();
                    newMatchDay.setNumber(actionForm.getMatchDayNumber());
                    newMatchDay.setSeason(currentSeason);
                    return matchDayRepository.save(newMatchDay);
                });

        Player player;
        if (actionForm.isNewPlayer()) {
            // Create new player - but first validate club exists
            Club club = clubRepository.findByName(actionForm.getClubName())
                    .orElseThrow(() -> new IllegalArgumentException("Club not found: " + actionForm.getClubName()));

            player = new Player();
            player.setName(actionForm.getPlayerName());
            player.setClub(club);
            player = playerRepository.save(player);
        } else {
            player = playerRepository.findById(actionForm.getPlayerId())
                    .orElseThrow(() -> new RuntimeException("Player not found"));
        }

        // Check if action already exists for this player and match day
        Action existingAction = actionRepository.findByPlayerIdAndMatchDayId(player.getId(), matchDay.getId())
                .orElse(null);

        if (existingAction != null) {
            // Update existing action
            existingAction.setGoals(actionForm.getGoals());
            existingAction.setAssists(actionForm.getAssists());
            actionRepository.save(existingAction);
        } else {
            // Create new action
            Action action = new Action();
            action.setPlayer(player);
            action.setMatchDay(matchDay);
            action.setGoals(actionForm.getGoals());
            action.setAssists(actionForm.getAssists());
            actionRepository.save(action);
        }
    }

    @Override
    public Action getActionById(Long actionId) {
        return actionRepository.findById(actionId)
                .orElseThrow(() -> new EntityNotFoundException("Action not found with id: " + actionId));
    }

    @Override
    @Transactional
    public void updateAction(Action action) {
        if (actionRepository.existsById(action.getId())) {
            actionRepository.save(action);
        } else {
            throw new EntityNotFoundException("Action not found with id: " + action.getId());
        }
    }

    @Override
    @Transactional
    public void deleteAction(Long actionId) {
        if (actionRepository.existsById(actionId)) {
            actionRepository.deleteById(actionId);
        } else {
            throw new EntityNotFoundException("Action not found with id: " + actionId);
        }
    }

    @Override
    public List<Action> getActionsByMatchDay(Integer matchDayNumber) {
        return actionRepository.findByMatchDayNumber(matchDayNumber);
    }

    @Override
    public List<Action> getActionsByPlayer(Long playerId) {
        return actionRepository.findByPlayerIdOrderByMatchDay(playerId);
    }

    @Override
    public Action findActionByPlayerAndMatchDay(Long playerId, Long matchDayId) {
        return actionRepository.findByPlayerIdAndMatchDayId(playerId, matchDayId).orElse(null);
    }

    @Override
    public ActionFormDTO getActionFormForEdit(Long actionId) {
        Action action = getActionById(actionId);

        ActionFormDTO form = new ActionFormDTO();
        form.setPlayerId(action.getPlayer().getId());
        form.setPlayerName(action.getPlayer().getName());
        form.setClubName(action.getPlayer().getClub().getName());
        form.setMatchDayNumber(action.getMatchDay().getNumber());
        form.setGoals(action.getGoals());
        form.setAssists(action.getAssists());
        form.setNewPlayer(false);

        return form;
    }

    @Override
    @Transactional
    public void updateActionFromForm(ActionFormDTO actionForm) {
        if (actionForm.getPlayerId() == null) {
            throw new IllegalArgumentException("Player ID is required for updating action");
        }

        // Get current season
        Season currentSeason = seasonRepository.findByIsCurrentTrue()
                .orElseThrow(() -> new IllegalStateException("No current season is set"));

        // Get match day
        MatchDay matchDay = matchDayRepository.findByNumberAndSeason(actionForm.getMatchDayNumber(), currentSeason)
                .orElseThrow(() -> new EntityNotFoundException("Match day not found"));

        // Find existing action
        Action action = actionRepository.findByPlayerIdAndMatchDayId(actionForm.getPlayerId(), matchDay.getId())
                .orElseThrow(() -> new EntityNotFoundException("Action not found for player and match day"));

        // Update action
        action.setGoals(actionForm.getGoals());
        action.setAssists(actionForm.getAssists());

        actionRepository.save(action);
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
                            (Long) result[5]       // totalPoints (now weighted)
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
                            (Long) result[5]       // totalPoints (now weighted)
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
    public Page<PlayerStatisticsDTO> getPlayerStatisticsPaginated(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, mapSortField(sortBy)));

        Page<Object[]> results = actionRepository.getPlayerStatisticsPaginated(pageable);
        return results.map(result -> {
            PlayerStatisticsDTO dto = new PlayerStatisticsDTO(
                    (Long) result[0],      // playerId
                    (String) result[1],    // playerName
                    (String) result[2],    // clubName
                    (Long) result[3],      // totalGoals
                    (Long) result[4],      // totalAssists
                    (Long) result[5]       // totalPoints (now weighted)
            );
            // Calculate streaks
            StreakResultDTO streaks = calculatePlayerStreaks(dto.getPlayerId());
            dto.setMaxGoalStreak(streaks.getMaxGoalStreak());
            dto.setMaxAssistStreak(streaks.getMaxAssistStreak());
            return dto;
        });
    }

    @Override
    public Page<PlayerStatisticsDTO> getPlayerStatisticsByClubPaginated(Long clubId, int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, mapSortField(sortBy)));

        Page<Object[]> results = actionRepository.getPlayerStatisticsByClubPaginated(clubId, pageable);
        return results.map(result -> {
            PlayerStatisticsDTO dto = new PlayerStatisticsDTO(
                    (Long) result[0],      // playerId
                    (String) result[1],    // playerName
                    (String) result[2],    // clubName
                    (Long) result[3],      // totalGoals
                    (Long) result[4],      // totalAssists
                    (Long) result[5]       // totalPoints (now weighted)
            );
            // Calculate streaks
            StreakResultDTO streaks = calculatePlayerStreaks(dto.getPlayerId());
            dto.setMaxGoalStreak(streaks.getMaxGoalStreak());
            dto.setMaxAssistStreak(streaks.getMaxAssistStreak());
            return dto;
        });
    }

    private String mapSortField(String sortBy) {
        return switch (sortBy) {
            case "goals" -> "totalGoals";
            case "assists" -> "totalAssists";
            case "points" -> "totalPoints";
            case "name" -> "playerName";
            case "club" -> "clubName";
            default -> "totalPoints";
        };
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

    @Override
    public List<StreakLeaderboardDTO> getLongestGoalStreaks(int limit) {
        List<Object[]> results = actionRepository.getLongestGoalStreaks();
        return results.stream()
                .limit(limit)
                .map(result -> {
                    StreakLeaderboardDTO dto = new StreakLeaderboardDTO(
                            (Long) result[0],        // playerId
                            (String) result[1],      // playerName
                            (String) result[2],      // clubName
                            ((Number) result[3]).intValue()      // streakLength
                    );
                    dto.setStreakType("GOALS");
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<StreakLeaderboardDTO> getLongestAssistStreaks(int limit) {
        List<Object[]> results = actionRepository.getLongestAssistStreaks();
        return results.stream()
                .limit(limit)
                .map(result -> {
                    StreakLeaderboardDTO dto = new StreakLeaderboardDTO(
                            (Long) result[0],              // playerId
                            (String) result[1],            // playerName
                            (String) result[2],            // clubName
                            ((Number) result[3]).intValue() // streakLength
                    );
                    dto.setStreakType("ASSISTS");
                    return dto;
                })
                .collect(Collectors.toList());
    }

}