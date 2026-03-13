package com.vbforge.footballstats.controller;

import com.vbforge.footballstats.dto.game.GameFormDTO;
import com.vbforge.footballstats.dto.game.GameUpdateDTO;
import com.vbforge.footballstats.entity.Club;
import com.vbforge.footballstats.entity.Game;
import com.vbforge.footballstats.entity.MatchDay;
import com.vbforge.footballstats.entity.Season;
import com.vbforge.footballstats.service.ClubService;
import com.vbforge.footballstats.service.GameService;
import com.vbforge.footballstats.service.MatchDayService;
import com.vbforge.footballstats.service.SeasonService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/games")
@Slf4j
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

        return "games/games";
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

        return "games/games_club";
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

        return "games/games_match_day";
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

        return "games/games_month";
    }

    @GetMapping("/scheduled")
    public String scheduledGames(Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());

        List<Game> scheduledGames = gameService.getScheduledGames();

        model.addAttribute("scheduledGames", scheduledGames);
        model.addAttribute("pageTitle", "Scheduled Games");

        return "games/games_scheduled";
    }

    @GetMapping("/finished")
    public String finishedGames(Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());

        List<Game> finishedGames = gameService.getFinishedGames();

        model.addAttribute("finishedGames", finishedGames);
        model.addAttribute("pageTitle", "Finished Games");

        return "games/games_finished";
    }

    @GetMapping("/add")
    public String gameForm(Model model) {
        Season season = seasonService.getCurrentSeason()
                .orElseThrow(() -> new RuntimeException("No current season found"));

        model.addAttribute("seasonName", season.getName());
        model.addAttribute("gameForm", new GameFormDTO());
        model.addAttribute("clubs", clubService.getAllClubs());
        return "games/games_add";
    }

    @PostMapping("/add-game")
    public String addGame(@Valid @ModelAttribute GameFormDTO gameFormDTO,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    "Please correct the form errors and try again");
            redirectAttributes.addFlashAttribute("gameForm", gameFormDTO);
            return "redirect:/games/add";
        }

        try {
            // Additional custom validation
            if (gameFormDTO.getHomeClub() != null &&
                    gameFormDTO.getHomeClub().equals(gameFormDTO.getAwayClub())) {
                redirectAttributes.addFlashAttribute("error",
                        "Home and away clubs must be different");
                redirectAttributes.addFlashAttribute("gameForm", gameFormDTO);
                return "redirect:/games/add";
            }

            gameService.saveGame(gameFormDTO);

            String successMessage = gameFormDTO.getStatus() != null &&
                    "FINISHED".equals(gameFormDTO.getStatus()) ?
                    String.format("Game result added: %s %d-%d %s",
                            gameFormDTO.getHomeClub(),
                            gameFormDTO.getHomeGoals(),
                            gameFormDTO.getAwayGoals(),
                            gameFormDTO.getAwayClub()) :
                    String.format("Game scheduled: %s vs %s",
                            gameFormDTO.getHomeClub(),
                            gameFormDTO.getAwayClub());

            redirectAttributes.addFlashAttribute("success", successMessage);

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("gameForm", gameFormDTO);
            return "redirect:/games/add";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error adding game: " + e.getMessage());
            redirectAttributes.addFlashAttribute("gameForm", gameFormDTO);
            return "redirect:/games/add";
        } catch (Exception e) {
            log.error("Unexpected error adding game", e);
            redirectAttributes.addFlashAttribute("error",
                    "An unexpected error occurred. Please try again.");
            redirectAttributes.addFlashAttribute("gameForm", gameFormDTO);
            return "redirect:/games/add";
        }

        return "redirect:/games";
    }

    @GetMapping("/edit/{gameId}")
    public String editGameForm(@PathVariable Long gameId, Model model) {
        Season season = seasonService.getCurrentSeason()
                .orElseThrow(() -> new RuntimeException("No current season found"));
        model.addAttribute("seasonName", season.getName());

        Optional<Game> gameOpt = gameService.getGameById(gameId);
        if (gameOpt.isEmpty()) {
            return "redirect:/games";
        }

        Game game = gameOpt.get();

        // Create form DTO with current game data
        GameUpdateDTO gameUpdateForm = new GameUpdateDTO(
                game.getId(),
                game.getHomeGoals(),
                game.getAwayGoals(),
                game.getStatus().toString()
        );

        model.addAttribute("game", game);
        model.addAttribute("gameUpdateForm", gameUpdateForm);

        return "games/games_edit";
    }

    @PostMapping("/edit/{gameId}")
    public String updateGame(@PathVariable Long gameId,
                             @ModelAttribute GameUpdateDTO gameUpdateForm,
                             RedirectAttributes redirectAttributes) {

        try {
            // Additional validation
            if (!gameUpdateForm.isValid()) {
                redirectAttributes.addFlashAttribute("error",
                        "Invalid input. Goals must be between 0 and 20, and status must be selected.");
                return "redirect:/games/edit/" + gameId;
            }

            Optional<Game> gameOpt = gameService.getGameById(gameId);
            if (gameOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Game not found.");
                return "redirect:/games";
            }

            Game game = gameOpt.get();

            // Update game data based on status
            Game.GameStatus newStatus = Game.GameStatus.valueOf(gameUpdateForm.getStatus());
            game.setStatus(newStatus);

            if (newStatus == Game.GameStatus.FINISHED) {
                game.setHomeGoals(gameUpdateForm.getHomeGoals());
                game.setAwayGoals(gameUpdateForm.getAwayGoals());
            } else {
                game.setHomeGoals(null);
                game.setAwayGoals(null);
            }

            // Save updated game
            gameService.updateGame(game);

            String resultMessage = gameUpdateForm.isFinished()
                    ? String.format("Game result updated: %s %d-%d %s",
                    game.getHomeClub().getName(),
                    game.getHomeGoals(),
                    game.getAwayGoals(),
                    game.getAwayClub().getName())
                    : "Game updated successfully";

            redirectAttributes.addFlashAttribute("success", resultMessage);

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/games/edit/" + gameId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error updating game: " + e.getMessage());
            return "redirect:/games/edit/" + gameId;
        } catch (Exception e) {
            log.error("Unexpected error updating game", e);
            redirectAttributes.addFlashAttribute("error",
                    "An unexpected error occurred. Please try again.");
            return "redirect:/games/edit/" + gameId;
        }

        return "redirect:/games";
    }

    // Quick update endpoint for AJAX calls
    @PostMapping("/edit/{gameId}/quick-finish")
    @ResponseBody
    public ResponseEntity<?> quickFinishGame(@PathVariable Long gameId,
                                             @RequestParam Integer homeGoals,
                                             @RequestParam Integer awayGoals) {
        try {
            // Validate goals
            if (homeGoals < 0 || awayGoals < 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Goals cannot be negative"
                ));
            }

            if (homeGoals > 20 || awayGoals > 20) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Goals cannot exceed 20"
                ));
            }

            Optional<Game> gameOpt = gameService.getGameById(gameId);
            if (gameOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Game game = gameOpt.get();
            game.setHomeGoals(homeGoals);
            game.setAwayGoals(awayGoals);
            game.setStatus(Game.GameStatus.FINISHED);

            gameService.updateGame(game);

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Game finished successfully",
                    "result", game.getResult()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error updating game: " + e.getMessage()
            ));
        }
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