package com.vbforge.footballstats.controller;

import com.vbforge.footballstats.dto.*;
import com.vbforge.footballstats.entity.Player;
import com.vbforge.footballstats.service.FootballStatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FootballStatsController {

    private FootballStatsService footballStatsService;

    public FootballStatsController(FootballStatsService footballStatsService) {
        this.footballStatsService = footballStatsService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("actionForm", new ActionFormDTO());
        model.addAttribute("clubs", footballStatsService.getAllClubs());
        model.addAttribute("players", footballStatsService.getAllPlayers());
        return "index";
    }

    @PostMapping("/add-action")
    public String addAction(@ModelAttribute ActionFormDTO actionForm,
                            RedirectAttributes redirectAttributes) {
        try {
            footballStatsService.saveAction(actionForm);
            redirectAttributes.addFlashAttribute("success",
                    "Action added successfully for " + actionForm.getPlayerName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error adding action: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/statistics")
    public String viewStatistics(Model model) {
        List<PlayerStatisticsDTO> stats = footballStatsService.getAllPlayerStatistics();
        model.addAttribute("playerStats", stats);
        model.addAttribute("clubs", footballStatsService.getAllClubs());

        // Calculate totals
        int totalGoals = stats.stream().mapToInt(PlayerStatisticsDTO::getTotalGoals).sum();
        int totalAssists = stats.stream().mapToInt(PlayerStatisticsDTO::getTotalAssists).sum();
        int totalPoints = stats.stream().mapToInt(PlayerStatisticsDTO::getTotalPoints).sum();

        model.addAttribute("totalGoals", totalGoals);
        model.addAttribute("totalAssists", totalAssists);
        model.addAttribute("totalPoints", totalPoints);
        model.addAttribute("activePlayers", stats.size());

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

        return "statistics";
    }

    @GetMapping("/statistics/club/{clubId}")
    public String viewClubStatistics(@PathVariable Long clubId, Model model) {
        List<PlayerStatisticsDTO> stats = footballStatsService.getPlayerStatisticsByClub(clubId);
        model.addAttribute("playerStats", stats);
        model.addAttribute("clubs", footballStatsService.getAllClubs());
        model.addAttribute("selectedClubId", clubId);

        // Calculate totals
        int totalGoals = stats.stream().mapToInt(PlayerStatisticsDTO::getTotalGoals).sum();
        int totalAssists = stats.stream().mapToInt(PlayerStatisticsDTO::getTotalAssists).sum();
        int totalPoints = stats.stream().mapToInt(PlayerStatisticsDTO::getTotalPoints).sum();

        model.addAttribute("totalGoals", totalGoals);
        model.addAttribute("totalAssists", totalAssists);
        model.addAttribute("totalPoints", totalPoints);
        model.addAttribute("activePlayers", stats.size());

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

        return "statistics";
    }

    @GetMapping("/player-streaks/{playerId}")
    @ResponseBody
    public StreakResultDTO getPlayerStreaks(@PathVariable Long playerId) {
        return footballStatsService.calculatePlayerStreaks(playerId);
    }

//    @GetMapping("/api/players/club/{clubId}")
//    @ResponseBody
//    public List<Player> getPlayersByClub(@PathVariable Long clubId) {
//        return footballStatsService.getPlayersByClub(clubId);
//    }

    @GetMapping("/api/players/club/{clubId}")
    @ResponseBody
    public List<PlayerDTO> getPlayersByClub(@PathVariable Long clubId) {
        return footballStatsService.getPlayersByClub(clubId)
                .stream()
                .map(p -> new PlayerDTO(p.getId(), p.getName()))
                .toList();
    }

    @GetMapping("/league")
    public String leagueTable(Model model) {
        List<ClubStandingsDTO> standings = footballStatsService.getClubStandings();
        model.addAttribute("standings", standings);
        return "league";
    }

    @GetMapping("/match-result")
    public String matchResultForm(Model model) {
        model.addAttribute("matchResultForm", new MatchResultFormDTO());
        model.addAttribute("clubs", footballStatsService.getAllClubs());
        return "match-result";
    }

    @PostMapping("/add-match-result")
    public String addMatchResult(@ModelAttribute MatchResultFormDTO matchResultForm,
                                 RedirectAttributes redirectAttributes) {
        try {
            footballStatsService.saveMatchResult(matchResultForm);
            redirectAttributes.addFlashAttribute("success",
                    "Match result added successfully for " +
                            (matchResultForm.isNewClub() ? matchResultForm.getClubName() : "selected club"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error adding match result: " + e.getMessage());
        }
        return "redirect:/match-result";
    }

}
