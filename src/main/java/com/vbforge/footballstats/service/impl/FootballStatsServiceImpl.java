package com.vbforge.footballstats.service.impl;

import com.vbforge.footballstats.dto.*;
import com.vbforge.footballstats.entity.*;
import com.vbforge.footballstats.repository.*;
import com.vbforge.footballstats.service.FootballStatsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FootballStatsServiceImpl implements FootballStatsService {

    private PlayerRepository playerRepository;
    private ClubRepository clubRepository;
    private MatchDayRepository matchDayRepository;
    private ActionRepository actionRepository;
    private MatchResultRepository matchResultRepository;

    public FootballStatsServiceImpl(PlayerRepository playerRepository,
                                    ClubRepository clubRepository,
                                    MatchDayRepository matchDayRepository,
                                    ActionRepository actionRepository,
                                    MatchResultRepository matchResultRepository) {
        this.playerRepository = playerRepository;
        this.clubRepository = clubRepository;
        this.matchDayRepository = matchDayRepository;
        this.actionRepository = actionRepository;
        this.matchResultRepository = matchResultRepository;
    }

    @Override
    public List<Club> getAllClubs() {
        return clubRepository.findAll();
    }

    @Override
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @Override
    public List<Player> getPlayersByClub(Long clubId) {
//        return playerRepository.findByClubId(clubId);
        return playerRepository.findByClub_Id(clubId);
    }

    @Override
    public void saveAction(ActionFormDTO actionForm) {
        // Get or create match day
        MatchDay matchDay = matchDayRepository.findByNumber(actionForm.getMatchDayNumber())
                .orElseGet(() -> {
                    MatchDay newMatchDay = new MatchDay();
                    newMatchDay.setNumber(actionForm.getMatchDayNumber());
                    return matchDayRepository.save(newMatchDay);
                });

        Player player;
        if (actionForm.isNewPlayer()) {
            // Create new player
            Club club = clubRepository.findByName(actionForm.getClubName())
                    .orElseGet(() -> {
                        Club newClub = new Club();
                        newClub.setName(actionForm.getClubName());
                        return clubRepository.save(newClub);
                    });

            player = new Player();
            player.setName(actionForm.getPlayerName());
            player.setClub(club);
            player = playerRepository.save(player);
        } else {
            player = playerRepository.findById(actionForm.getPlayerId())
                    .orElseThrow(() -> new RuntimeException("Player not found"));
        }

        // Create action
        Action action = new Action();
        action.setPlayer(player);
        action.setMatchDay(matchDay);
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

    @Override
    public Optional<Player> findPlayer(Long playerId) {
        return playerRepository.findById(playerId);
    }

    public void saveMatchResult(MatchResultFormDTO matchResultForm) {
        // Get or create match day
        MatchDay matchDay = matchDayRepository.findByNumber(matchResultForm.getMatchDayNumber())
                .orElseGet(() -> {
                    MatchDay newMatchDay = new MatchDay();
                    newMatchDay.setNumber(matchResultForm.getMatchDayNumber());
                    return matchDayRepository.save(newMatchDay);
                });

        Club club;
        if (matchResultForm.isNewClub()) {
            // Create new club
            club = clubRepository.findByName(matchResultForm.getClubName())
                    .orElseGet(() -> {
                        Club newClub = new Club();
                        newClub.setName(matchResultForm.getClubName());
                        return clubRepository.save(newClub);
                    });
        } else {
            club = clubRepository.findById(matchResultForm.getClubId())
                    .orElseThrow(() -> new RuntimeException("Club not found"));
        }

        // Create match result
        MatchResult matchResult = new MatchResult();
        matchResult.setClub(club);
        matchResult.setMatchDay(matchDay);
        matchResult.setPoints(matchResultForm.getPoints());
        matchResult.setGoalsFor(matchResultForm.getGoalsFor());
        matchResult.setGoalsAgainst(matchResultForm.getGoalsAgainst());

        matchResultRepository.save(matchResult);
    }

//    public List<ClubStandingsDTO> getClubStandings() {
//        List<Object[]> results = matchResultRepository.getClubStandings();
//        return results.stream()
//                .map(result -> new ClubStandingsDTO(
//                        (Long) result[0],      // clubId
//                        (String) result[1],    // clubName
//                        (Long) result[2],      // matchesPlayed
//                        (Long) result[3],      // wins
//                        (Long) result[4],      // draws
//                        (Long) result[5],      // defeats
//                        (Long) result[6],      // totalPoints
//                        (Long) result[7],      // goalsFor
//                        (Long) result[8]       // goalsAgainst
//                ))
//                .collect(Collectors.toList());
//    }

    public List<ClubStandingsDTO> getClubStandings() {
        return matchResultRepository.getClubStandings(); // No mapping needed!
    }

    public List<MatchResult> getClubMatchResults(Long clubId) {
        return matchResultRepository.findByClubIdOrderByMatchDay(clubId);
    }

    public List<MatchResult> getMatchDayResults(Integer matchDayNumber) {
        return matchResultRepository.findByMatchDayNumber(matchDayNumber);
    }

}
