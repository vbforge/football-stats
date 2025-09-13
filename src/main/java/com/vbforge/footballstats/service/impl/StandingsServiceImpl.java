package com.vbforge.footballstats.service.impl;

import com.vbforge.footballstats.dto.ClubStandingsDTO;
import com.vbforge.footballstats.entity.Club;
import com.vbforge.footballstats.entity.Game;
import com.vbforge.footballstats.entity.Season;
import com.vbforge.footballstats.repository.ClubRepository;
import com.vbforge.footballstats.repository.GameRepository;
import com.vbforge.footballstats.repository.SeasonRepository;
import com.vbforge.footballstats.service.StandingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StandingsServiceImpl implements StandingsService {

    private final GameRepository gameRepository;
    private final ClubRepository clubRepository;
    private final SeasonRepository seasonRepository;

    public StandingsServiceImpl(GameRepository gameRepository, ClubRepository clubRepository, SeasonRepository seasonRepository) {
        this.gameRepository = gameRepository;
        this.clubRepository = clubRepository;
        this.seasonRepository = seasonRepository;
    }


    @Override
    public List<ClubStandingsDTO> getCurrentSeasonStandings() {
        Season currentSeason = seasonRepository.findByIsCurrentTrue()
                .orElseThrow(() -> new IllegalStateException("No current season is set"));

        return getStandingsBySeason(currentSeason.getId());
    }

    @Override
    public List<ClubStandingsDTO> getStandingsBySeason(Long seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new IllegalArgumentException("Season not found with id: " + seasonId));

        // Get all finished games for this season
        List<Game> finishedGames = gameRepository.findBySeasonAndStatusOrderByGameDateAsc(
                season, Game.GameStatus.FINISHED);

        // Get all clubs
        List<Club> allClubs = clubRepository.findAll();

        // Initialize club statistics map
        Map<Long, ClubStandingsDTO> clubStats = new HashMap<>();

        // Initialize all clubs with zero stats
        for (Club club : allClubs) {
            clubStats.put(club.getId(), new ClubStandingsDTO(
                    club.getId(),
                    club.getName(),
                    club.getLogoPath(),
                    0L, 0L, 0L, 0L, 0L, 0L, 0L // all zeros initially
            ));
        }

        // Calculate statistics from games
        for (Game game : finishedGames) {
            Long homeClubId = game.getHomeClub().getId();
            Long awayClubId = game.getAwayClub().getId();

            ClubStandingsDTO homeStats = clubStats.get(homeClubId);
            ClubStandingsDTO awayStats = clubStats.get(awayClubId);

            if (homeStats != null && awayStats != null) {
                // Update matches played
                homeStats.setMatchesPlayed(homeStats.getMatchesPlayed() + 1);
                awayStats.setMatchesPlayed(awayStats.getMatchesPlayed() + 1);

                // Update goals
                homeStats.setGoalsFor(homeStats.getGoalsFor() + game.getHomeGoals());
                homeStats.setGoalsAgainst(homeStats.getGoalsAgainst() + game.getAwayGoals());
                awayStats.setGoalsFor(awayStats.getGoalsFor() + game.getAwayGoals());
                awayStats.setGoalsAgainst(awayStats.getGoalsAgainst() + game.getHomeGoals());

                // Determine result and update wins/draws/defeats and points
                if (game.getHomeGoals() > game.getAwayGoals()) {
                    // Home win
                    homeStats.setWins(homeStats.getWins() + 1);
                    homeStats.setTotalPoints(homeStats.getTotalPoints() + 3);
                    awayStats.setDefeats(awayStats.getDefeats() + 1);
                } else if (game.getAwayGoals() > game.getHomeGoals()) {
                    // Away win
                    awayStats.setWins(awayStats.getWins() + 1);
                    awayStats.setTotalPoints(awayStats.getTotalPoints() + 3);
                    homeStats.setDefeats(homeStats.getDefeats() + 1);
                } else {
                    // Draw
                    homeStats.setDraws(homeStats.getDraws() + 1);
                    homeStats.setTotalPoints(homeStats.getTotalPoints() + 1);
                    awayStats.setDraws(awayStats.getDraws() + 1);
                    awayStats.setTotalPoints(awayStats.getTotalPoints() + 1);
                }

                // Update goal difference
                homeStats.setGoalDifference(homeStats.getGoalsFor() - homeStats.getGoalsAgainst());
                awayStats.setGoalDifference(awayStats.getGoalsFor() - awayStats.getGoalsAgainst());
            }
        }

        // Convert to list and sort by league table rules
        return clubStats.values().stream()
                .sorted((a, b) -> {
                    // 1. Points (descending)
                    int pointsComparison = Integer.compare(b.getTotalPoints(), a.getTotalPoints());
                    if (pointsComparison != 0) return pointsComparison;

                    // 2. Goal difference (descending)
                    int goalDiffComparison = Integer.compare(b.getGoalDifference(), a.getGoalDifference());
                    if (goalDiffComparison != 0) return goalDiffComparison;

                    // 3. Goals for (descending)
                    int goalsForComparison = Integer.compare(b.getGoalsFor(), a.getGoalsFor());
                    if (goalsForComparison != 0) return goalsForComparison;

                    // 4. Club name (ascending) as tiebreaker
                    return a.getClubName().compareTo(b.getClubName());
                })
                .collect(Collectors.toList());
    }

    @Override
    public ClubStandingsDTO getClubStandingsForCurrentSeason(Long clubId) {
        List<ClubStandingsDTO> allStandings = getCurrentSeasonStandings();
        return allStandings.stream()
                .filter(standing -> standing.getClubId().equals(clubId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public int getClubPositionInCurrentSeason(Long clubId) {
        List<ClubStandingsDTO> standings = getCurrentSeasonStandings();
        for (int i = 0; i < standings.size(); i++) {
            if (standings.get(i).getClubId().equals(clubId)) {
                return i + 1; // Position is 1-based
            }
        }
        return 0; // Club not found
    }
}
