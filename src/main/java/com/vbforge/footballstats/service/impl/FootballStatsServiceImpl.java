package com.vbforge.footballstats.service.impl;

import com.vbforge.footballstats.dto.*;
import com.vbforge.footballstats.entity.*;
import com.vbforge.footballstats.repository.*;
import com.vbforge.footballstats.service.FootballStatsService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    public List<ClubStandingsDTO> getClubStandings() {
        return matchResultRepository.getClubStandings();
    }

    @Override
    public ClubDetailDTO getClubDetail(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new EntityNotFoundException("Club not found with id: " + clubId));

        ClubDetailDTO dto = new ClubDetailDTO();

        // Basic club info
        dto.setId(club.getId());
        dto.setName(club.getName());
        dto.setCoach(club.getCoach());
        dto.setLogoPath(club.getLogoPath());
        dto.setCity(club.getCity());
        dto.setFoundedYear(club.getFoundedYear());
        dto.setStadium(club.getStadium());
        dto.setStadiumCapacity(club.getStadiumCapacity());
        dto.setDescription(club.getDescription());
        dto.setWebsite(club.getWebsite());
        dto.setNickname(club.getNickname());
        dto.setStadiumImagePath(club.getStadiumImagePath());
        dto.setPrimaryColor(club.getPrimaryColor());
        dto.setSecondaryColor(club.getSecondaryColor());

        // Get club statistics from league standings
        List<ClubStandingsDTO> standings = getClubStandings();
        ClubStandingsDTO clubStanding = standings.stream()
                .filter(s -> s.getClubId().equals(clubId))
                .findFirst()
                .orElse(null);

        if (clubStanding != null) {
            dto.setMatchesPlayed(clubStanding.getMatchesPlayed());
            dto.setWins(clubStanding.getWins());
            dto.setDraws(clubStanding.getDraws());
            dto.setDefeats(clubStanding.getDefeats());
            dto.setTotalPoints(clubStanding.getTotalPoints());
            dto.setGoalsFor(clubStanding.getGoalsFor());
            dto.setGoalsAgainst(clubStanding.getGoalsAgainst());
            dto.setGoalDifference(clubStanding.getGoalDifference());

            // Calculate current position
            dto.setCurrentPosition(standings.indexOf(clubStanding) + 1);
        }

        // Get player statistics using existing methods
        dto.setTopScorers(getClubTopScorers(clubId, 5));
        dto.setTopAssisters(getClubTopAssisters(clubId, 5));

        // Calculate total players for this club
        List<PlayerStatisticsDTO> allClubPlayers = getPlayerStatisticsByClub(clubId);
        dto.setTotalPlayers(allClubPlayers.size());

        return dto;
    }

    @Override
    public Club getClubById(Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new EntityNotFoundException("Club not found with id: " + clubId));
    }

    @Override
    public void updateClub(Club club) {
        if (clubRepository.existsById(club.getId())) {
            clubRepository.save(club);
        } else {
            throw new EntityNotFoundException("Club not found with id: " + club.getId());
        }
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
}
