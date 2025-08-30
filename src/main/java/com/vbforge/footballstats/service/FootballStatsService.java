package com.vbforge.footballstats.service;

import com.vbforge.footballstats.dto.*;
import com.vbforge.footballstats.entity.Club;
import com.vbforge.footballstats.entity.MatchResult;
import com.vbforge.footballstats.entity.Player;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface FootballStatsService {
    List<Club> getAllClubs();
    List<Player> getAllPlayers();
    List<Player> getPlayersByClub(Long clubId);
    void saveAction(ActionFormDTO actionForm);
    List<PlayerStatisticsDTO> getAllPlayerStatistics();
    List<PlayerStatisticsDTO> getPlayerStatisticsByClub(Long clubId);
    StreakResultDTO calculatePlayerStreaks(Long playerId);
    void saveMatchResult(MatchResultFormDTO matchResultForm);
    List<ClubStandingsDTO> getClubStandings();
    ClubDetailDTO getClubDetail(Long clubId);
    Club getClubById(Long clubId);
    void updateClub(Club club);

    List<PlayerStatsDTO> getClubTopScorers(Long clubId, int limit);
    List<PlayerStatsDTO> getClubTopAssisters(Long clubId, int limit);

    PlayerStatisticsDTO getPlayerDetail(Long playerId);
}
