package com.vbforge.footballstats.controller;

import com.vbforge.footballstats.dto.GameFormDTO;
import com.vbforge.footballstats.dto.MatchResultFormDTO;
import com.vbforge.footballstats.entity.Club;
import com.vbforge.footballstats.entity.Game;
import com.vbforge.footballstats.entity.MatchDay;
import com.vbforge.footballstats.service.FootballStatsService;
import com.vbforge.footballstats.service.GameService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;
    private final FootballStatsService footballStatsService;

    public GameController(GameService gameService, FootballStatsService footballStatsService) {
        this.gameService = gameService;
        this.footballStatsService = footballStatsService;
    }

    @GetMapping
    public String gamesCalendar(Model model) {
        List<Game> allGames = gameService.getAllGames();
        long finishedCount = gameService.getFinishedGamesCount();
        long scheduledCount = gameService.getScheduledGamesCount();

        model.addAttribute("games", allGames);
        model.addAttribute("finishedCount", finishedCount);
        model.addAttribute("scheduledCount", scheduledCount);
        model.addAttribute("pageTitle", "Games Calendar");

        return "games/calendar";
    }

    @GetMapping("/club/{clubId}")
    public String gamesByClub(@PathVariable Long clubId, Model model) {
        Optional<Club> clubOpt = Optional.ofNullable(footballStatsService.getClubById(clubId));
        if (clubOpt.isEmpty()) {
            return "redirect:/games";
        }

        Club club = clubOpt.get();
        Club currentClub = clubOpt.get();
        if(currentClub.getId() == club.getId()){

        }
        List<Game> clubGames = gameService.getGamesByClub(club);

        model.addAttribute("games", clubGames);
        model.addAttribute("club", club);
        model.addAttribute("currentClub", currentClub);
        model.addAttribute("pageTitle", "Games for " + club.getName());

        return "games/club-games";
    }

    @GetMapping("/matchday/{matchDayId}")
    public String gamesByMatchDay(@PathVariable Long matchDayId, Model model) {
        Optional<MatchDay> matchDayOpt = footballStatsService.getMatchDayById(matchDayId);
        if (matchDayOpt.isEmpty()) {
            return "redirect:/games";
        }

        MatchDay matchDay = matchDayOpt.get();
        List<Game> matchDayGames = gameService.getGamesByMatchDay(matchDay);

        model.addAttribute("games", matchDayGames);
        model.addAttribute("matchDay", matchDay);
        model.addAttribute("pageTitle", "Match Day " + matchDay.getNumber());

        return "games/matchday-games";
    }

    @GetMapping("/month")
    public String gamesByMonth(@RequestParam(required = false) String month, Model model) {
        YearMonth selectedMonth = month != null ? YearMonth.parse(month) : YearMonth.now();
        List<Game> monthGames = gameService.getGamesByMonth(selectedMonth);

        model.addAttribute("games", monthGames);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("pageTitle", "Games for " + selectedMonth.getMonth() + " " + selectedMonth.getYear());

        return "games/month-games";
    }

    @GetMapping("/scheduled")
    public String scheduledGames(Model model) {
        List<Game> scheduledGames = gameService.getScheduledGames();

        model.addAttribute("games", scheduledGames);
        model.addAttribute("pageTitle", "Scheduled Games");

        return "games/scheduled";
    }

    @GetMapping("/finished")
    public String finishedGames(Model model) {
        List<Game> finishedGames = gameService.getFinishedGames();

        model.addAttribute("games", finishedGames);
        model.addAttribute("pageTitle", "Finished Games");

        return "games/finished";
    }

    @GetMapping("/add")
    public String gameForm(Model model) {
        model.addAttribute("gameForm", new GameFormDTO());
        model.addAttribute("clubs", footballStatsService.getAllClubs());
        return "games/add-game";
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
        return "redirect:/games/add";
    }

}
