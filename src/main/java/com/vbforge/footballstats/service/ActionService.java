package com.vbforge.footballstats.service;

import com.vbforge.footballstats.dto.action.*;
import com.vbforge.footballstats.entity.Action;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ActionService {

    // Basic CRUD operations
    void saveAction(ActionFormDTO actionForm);
    Action getActionById(Long actionId);
    void updateAction(Action action);
    void deleteAction(Long actionId);

    // Find operations
    List<Action> getActionsByMatchDay(Integer matchDayNumber);
    List<Action> getActionsByPlayer(Long playerId);
    Action findActionByPlayerAndMatchDay(Long playerId, Long matchDayId);

    // Edit specific operations
    ActionFormDTO getActionFormForEdit(Long actionId);
    void updateActionFromForm(ActionFormDTO actionForm);


    // Statistics operations
    List<PlayerStatisticsDTO> getAllPlayerStatistics();
    List<PlayerStatisticsDTO> getPlayerStatisticsByClub(Long clubId);
    Page<PlayerStatisticsDTO> getPlayerStatisticsPaginated(int page, int size, String sortBy, String sortDir);
    Page<PlayerStatisticsDTO> getPlayerStatisticsByClubPaginated(Long clubId, int page, int size, String sortBy, String sortDir);


    // Top performers
    List<PlayerStatsDTO> getClubTopScorers(Long clubId, int limit);
    List<PlayerStatsDTO> getClubTopAssisters(Long clubId, int limit);

    // Streak operations
    StreakResultDTO calculatePlayerStreaks(Long playerId);
    List<StreakLeaderboardDTO> getLongestGoalStreaks(int limit);
    List<StreakLeaderboardDTO> getLongestAssistStreaks(int limit);
}
