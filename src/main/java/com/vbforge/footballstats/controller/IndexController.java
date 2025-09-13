package com.vbforge.footballstats.controller;

import com.vbforge.footballstats.dto.ClubStandingsDTO;
import com.vbforge.footballstats.dto.PlayerStatisticsDTO;
import com.vbforge.footballstats.entity.City;
import com.vbforge.footballstats.entity.Season;
import com.vbforge.footballstats.service.CityService;
import com.vbforge.footballstats.service.PlayerService;
import com.vbforge.footballstats.service.SeasonService;
import com.vbforge.footballstats.service.StandingsService;
import com.vbforge.footballstats.entity.Game;
import com.vbforge.footballstats.entity.MatchDay;
import com.vbforge.footballstats.service.GameService;
import com.vbforge.footballstats.service.MatchDayService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class IndexController {

    private final SeasonService seasonService;
    private final CityService cityService;
    private final StandingsService standingsService;
    private final PlayerService playerService;
    private final GameService gameService;
    private final MatchDayService matchDayService;

    public IndexController(SeasonService seasonService,
                           CityService cityService,
                           StandingsService standingsService,
                           PlayerService playerService,
                           GameService gameService,
                           MatchDayService matchDayService) {
        this.seasonService = seasonService;
        this.cityService = cityService;
        this.standingsService = standingsService;
        this.playerService = playerService;
        this.gameService = gameService;
        this.matchDayService = matchDayService;
    }

    @GetMapping("/")
    public String home(Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());

        List<City> cities = cityService.getAllCities();

        int totalClubs = cities != null
                ? cities.stream()
                .mapToInt(c -> c.getClubs() != null ? c.getClubs().size() : 0)
                .sum()
                : 0;

        model.addAttribute("totalClubs", totalClubs);

        model.addAttribute("cities", cities);
        assert cities != null;
        model.addAttribute("totalCities", cities.size());

        List<ClubStandingsDTO> standings = standingsService.getCurrentSeasonStandings();
        model.addAttribute("standings", standings);

        List<PlayerStatisticsDTO> stats = playerService.getAllPlayerStatistics();
        // Top goal scorers (only players with goals > 0)
        List<PlayerStatisticsDTO> topScorers = stats.stream()
                .filter(stat -> stat.getTotalGoals() > 0)
                .sorted((a, b) -> b.getTotalGoals().compareTo(a.getTotalGoals()))
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("topScorers", topScorers);

        // Top assist providers (only players with assists > 0)
        List<PlayerStatisticsDTO> topAssistProviders = stats.stream()
                .filter(stat -> stat.getTotalAssists() > 0)
                .sorted((a, b) -> b.getTotalAssists().compareTo(a.getTotalAssists()))
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("topAssistProviders", topAssistProviders);

        addMatchDayData(model);

        return "index";
    }

    // Private helper method to add match day data
    private void addMatchDayData(Model model) {
        try {
            // Get the latest finished match day
            MatchDay lastFinishedMatchDay = getLastFinishedMatchDay();
            if (lastFinishedMatchDay != null) {
                List<Game> lastMatchDayResults = gameService.getGamesByMatchDay(lastFinishedMatchDay)
                        .stream()
                        .filter(Game::isFinished)
                        .sorted(Comparator.comparing(Game::getGameDate))
                        .collect(Collectors.toList());

                model.addAttribute("lastMatchDay", lastFinishedMatchDay);
                model.addAttribute("lastMatchDayResults", lastMatchDayResults);
            } else {
                model.addAttribute("lastMatchDay", null);
                model.addAttribute("lastMatchDayResults", Collections.emptyList());
            }

            // Get the next scheduled match day
            MatchDay nextScheduledMatchDay = getNextScheduledMatchDay();
            if (nextScheduledMatchDay != null) {
                List<Game> nextMatchDaySchedule = gameService.getGamesByMatchDay(nextScheduledMatchDay)
                        .stream()
                        .filter(Game::isScheduled)
                        .sorted(Comparator.comparing(Game::getGameDate))
                        .collect(Collectors.toList());

                model.addAttribute("nextMatchDay", nextScheduledMatchDay);
                model.addAttribute("nextMatchDaySchedule", nextMatchDaySchedule);
            } else {
                model.addAttribute("nextMatchDay", null);
                model.addAttribute("nextMatchDaySchedule", Collections.emptyList());
            }

        } catch (Exception e) {
            // Log the error and provide empty data
            System.err.println("Error loading match day data: " + e.getMessage());
            model.addAttribute("lastMatchDay", null);
            model.addAttribute("lastMatchDayResults", Collections.emptyList());
            model.addAttribute("nextMatchDay", null);
            model.addAttribute("nextMatchDaySchedule", Collections.emptyList());
        }
    }

    // Helper method to get the last finished match day
    private MatchDay getLastFinishedMatchDay() {
        List<Game> finishedGames = gameService.getFinishedGames();

        if (finishedGames.isEmpty()) {
            return null;
        }

        // Get the most recent match day with finished games
        return finishedGames.stream()
                .map(Game::getMatchDay)
                .distinct()
                .max(Comparator.comparing(MatchDay::getNumber))
                .orElse(null);
    }

    // Helper method to get the next scheduled match day
    private MatchDay getNextScheduledMatchDay() {
        List<Game> scheduledGames = gameService.getScheduledGames();

        if (scheduledGames.isEmpty()) {
            return null;
        }

        // Get the earliest match day with scheduled games
        return scheduledGames.stream()
                .map(Game::getMatchDay)
                .distinct()
                .min(Comparator.comparing(MatchDay::getNumber))
                .orElse(null);
    }

    // Alternative approach: If you want to get match days by current date instead of status
    private MatchDay getLastFinishedMatchDayByDate() {
        LocalDate today = LocalDate.now();

        return gameService.getAllGames().stream()
                .filter(game -> game.getGameDate().isBefore(today) && game.isFinished())
                .map(Game::getMatchDay)
                .distinct()
                .max(Comparator.comparing(MatchDay::getNumber))
                .orElse(null);
    }

    private MatchDay getNextScheduledMatchDayByDate() {
        LocalDate today = LocalDate.now();

        return gameService.getAllGames().stream()
                .filter(game -> game.getGameDate().isAfter(today) || game.getGameDate().isEqual(today))
                .filter(Game::isScheduled)
                .map(Game::getMatchDay)
                .distinct()
                .min(Comparator.comparing(MatchDay::getNumber))
                .orElse(null);
    }

}
