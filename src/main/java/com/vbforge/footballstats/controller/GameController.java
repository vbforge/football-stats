package com.vbforge.footballstats.controller;

import com.vbforge.footballstats.dto.GameFormDTO;
import com.vbforge.footballstats.entity.Club;
import com.vbforge.footballstats.entity.Game;
import com.vbforge.footballstats.entity.MatchDay;
import com.vbforge.footballstats.entity.Season;
import com.vbforge.footballstats.service.ClubService;
import com.vbforge.footballstats.service.GameService;
import com.vbforge.footballstats.service.MatchDayService;
import com.vbforge.footballstats.service.SeasonService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/games")
public class GameController {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final GameService gameService;
    private final ClubService clubService;
    private final MatchDayService matchDayService;
    private final SeasonService seasonService;

    public GameController(GameService gameService,
                          ClubService clubService,
                          MatchDayService matchDayService,
                          SeasonService seasonService) {
        this.gameService = gameService;
        this.clubService = clubService;
        this.matchDayService = matchDayService;
        this.seasonService = seasonService;
    }

    @GetMapping
    public String games(Model model,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) Long clubId,
                        @RequestParam(required = false) Integer matchDay,
                        @RequestParam(required = false) String month,
                        @RequestParam(required = false) Integer year,
                        @RequestParam(required = false) String sort,
                        @RequestParam(required = false) String order) {

        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());
        model.addAttribute("pageTitle", "Games Management");

        // Get all games and apply filters
        List<Game> allGames = gameService.getAllGames();
        List<Game> filteredGames = applyFilters(allGames, status, clubId, matchDay, month, year);

        // Apply sorting
        filteredGames = applySorting(filteredGames, sort, order);

        model.addAttribute("games", filteredGames);

        // Stats
        long finishedCount = gameService.getFinishedGamesCount();
        model.addAttribute("finishedCount", finishedCount);
        long scheduledCount = gameService.getScheduledGamesCount();
        model.addAttribute("scheduledCount", scheduledCount);

        // Data for dropdowns
        model.addAttribute("clubs", clubService.getAllClubs());
        model.addAttribute("availableMonths", getAvailableMonths(allGames));
        model.addAttribute("availableYears", getAvailableYears(allGames));

        // Selected values for display
        if (clubId != null) {
            Club selectedClub = clubService.getClubById(clubId);
            model.addAttribute("selectedClub", selectedClub);
        }

        return "games";
    }

    @GetMapping("/club/{clubId}")
    public String gamesByClub(@PathVariable Long clubId, Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());

        Optional<Club> clubOpt = Optional.ofNullable(clubService.getClubById(clubId));
        if (clubOpt.isEmpty()) {
            return "redirect:/games";
        }

        Club club = clubOpt.get();
        List<Game> clubGames = gameService.getGamesByClub(club);

        model.addAttribute("games", clubGames);
        model.addAttribute("club", club);
        model.addAttribute("pageTitle", "Games for " + club.getName());

        return "club-games";
    }

    @GetMapping("/match-day/{matchDayId}")
    public String gamesByMatchDay(@PathVariable Long matchDayId, Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());

        Optional<MatchDay> matchDayOpt = matchDayService.getMatchDayById(matchDayId);
        if (matchDayOpt.isEmpty()) {
            return "redirect:/games";
        }

        MatchDay matchDay = matchDayOpt.get();
        List<Game> matchDayGames = gameService.getGamesByMatchDay(matchDay);

        model.addAttribute("games", matchDayGames);
        model.addAttribute("matchDay", matchDay);
        model.addAttribute("pageTitle", "Match Day " + matchDay.getNumber());

        return "games_match_day";
    }

    @GetMapping("/month")
    public String gamesByMonth(@RequestParam(required = false) String month, Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());

        YearMonth selectedMonth = month != null ? YearMonth.parse(month) : YearMonth.now();
        List<Game> monthGames = gameService.getGamesByMonth(selectedMonth);

        model.addAttribute("games", monthGames);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("pageTitle", "Games for " + selectedMonth.getMonth() + " " + selectedMonth.getYear());

        return "games_month";
    }

    @GetMapping("/scheduled")
    public String scheduledGames(Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());

        List<Game> scheduledGames = gameService.getScheduledGames();

        model.addAttribute("scheduledGames", scheduledGames);
        model.addAttribute("pageTitle", "Scheduled Games");

        return "games_scheduled";
    }

    @GetMapping("/finished")
    public String finishedGames(Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());

        List<Game> finishedGames = gameService.getFinishedGames();

        model.addAttribute("finishedGames", finishedGames);
        model.addAttribute("pageTitle", "Finished Games");

        return "games_finished";
    }

    @GetMapping("/add")
    public String gameForm(Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());
        model.addAttribute("gameForm", new GameFormDTO());
        model.addAttribute("clubs", clubService.getAllClubs());
        return "add-game";
    }

    @PostMapping("/add-game")
    public String addGame(@ModelAttribute GameFormDTO gameFormDTO,
                          RedirectAttributes redirectAttributes) {
        try {
            gameService.saveGame(gameFormDTO);
            redirectAttributes.addFlashAttribute("success",
                    "Game added successfully for " + gameFormDTO.getHomeClub() + " vs " + gameFormDTO.getAwayClub());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error adding match result: " + e.getMessage());
        }
        return "redirect:/games";
    }

    // Private helper methods
    private List<Game> applyFilters(List<Game> games, String status, Long clubId,
                                    Integer matchDay, String month, Integer year) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        return games.stream()
                .filter(game -> status == null || status.isEmpty() ||
                        game.getStatus().toString().equals(status))
                .filter(game -> clubId == null ||
                        game.getHomeClub().getId().equals(clubId) ||
                        game.getAwayClub().getId().equals(clubId))
                .filter(game -> matchDay == null ||
                        game.getMatchDay().getNumber().equals(matchDay))
                .filter(game -> month == null || month.isEmpty() ||
                        game.getGameDate().format(formatter).equals(month))
                .filter(game -> year == null ||
                        game.getGameDate().getYear() == year)
                .collect(Collectors.toList());
    }

    private List<Game> applySorting(List<Game> games, String sort, String order) {
        if (sort == null || sort.isEmpty()) {
            sort = "date"; // default sort
        }
        if (order == null || order.isEmpty()) {
            order = "asc"; // default order
        }

        Comparator<Game> comparator = switch (sort) {
            case "date" -> Comparator.comparing(Game::getGameDate);
            case "homeTeam" -> Comparator.comparing(game -> game.getHomeClub().getName());
            case "awayTeam" -> Comparator.comparing(game -> game.getAwayClub().getName());
            case "status" -> Comparator.comparing(Game::getStatus);
            case "matchDay" -> Comparator.comparing(game -> game.getMatchDay().getNumber());
            default -> Comparator.comparing(Game::getGameDate);
        };

        if ("desc".equals(order)) {
            comparator = comparator.reversed();
        }

        return games.stream().sorted(comparator).collect(Collectors.toList());
    }

    private List<String> getAvailableMonths(List<Game> games) {
        return games.stream()
                .map(game -> game.getGameDate().format(MONTH_FORMATTER))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private List<Integer> getAvailableYears(List<Game> games) {
        return games.stream()
                .map(game -> game.getGameDate().getYear())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}