package com.vbforge.footballstats.controller;

import com.vbforge.footballstats.dto.action.ActionFormDTO;
import com.vbforge.footballstats.dto.action.PlayerStatisticsDTO;
import com.vbforge.footballstats.dto.action.StreakLeaderboardDTO;
import com.vbforge.footballstats.dto.action.StreakResultDTO;
import com.vbforge.footballstats.dto.player.PlayerDTO;
import com.vbforge.footballstats.entity.Action;
import com.vbforge.footballstats.entity.Season;
import com.vbforge.footballstats.service.ActionService;
import com.vbforge.footballstats.service.ClubService;
import com.vbforge.footballstats.service.PlayerService;
import com.vbforge.footballstats.service.SeasonService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/actions")
public class ActionController {

    private final ClubService clubService;
    private final PlayerService playerService;
    private final ActionService actionService;
    private final SeasonService seasonService;

    public ActionController(ClubService clubService,
                            PlayerService playerService,
                            ActionService actionService,
                            SeasonService seasonService) {
        this.clubService = clubService;
        this.playerService = playerService;
        this.actionService = actionService;
        this.seasonService = seasonService;
    }

    @GetMapping("")
    public String viewActions(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size,
                              @RequestParam(defaultValue = "points") String sortBy,
                              @RequestParam(defaultValue = "desc") String sortDir,
                              @RequestParam(required = false) Long clubId,
                              Model model) {

        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());

        Page<PlayerStatisticsDTO> statsPage;
        if (clubId != null) {
            statsPage = actionService.getPlayerStatisticsByClubPaginated(clubId, page, size, sortBy, sortDir);
            model.addAttribute("selectedClubId", clubId);
        } else {
            statsPage = actionService.getPlayerStatisticsPaginated(page, size, sortBy, sortDir);
        }

        model.addAttribute("statsPage", statsPage);
        model.addAttribute("playerStats", statsPage.getContent());
        model.addAttribute("clubs", clubService.getAllClubs());

        // Calculate totals from current page
        List<PlayerStatisticsDTO> stats = statsPage.getContent();
        int totalGoals = stats.stream().mapToInt(PlayerStatisticsDTO::getTotalGoals).sum();
        int totalAssists = stats.stream().mapToInt(PlayerStatisticsDTO::getTotalAssists).sum();
        int totalPoints = stats.stream().mapToInt(PlayerStatisticsDTO::getTotalPoints).sum();

        model.addAttribute("totalGoals", totalGoals);
        model.addAttribute("totalAssists", totalAssists);
        model.addAttribute("totalPoints", totalPoints);
        model.addAttribute("activePlayers", statsPage.getTotalElements());

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

        // Pagination attributes
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "actions/action_statistics";
    }

//    // Main action form page
//    @GetMapping("/add")
//    public String action(Model model) {
//        Season season = seasonService.getCurrentSeason().orElseThrow();
//        model.addAttribute("seasonName", season.getName());
//        model.addAttribute("actionForm", new ActionFormDTO());
//        model.addAttribute("clubs", clubService.getAllClubs());
//        model.addAttribute("players", playerService.getAllPlayers());
//        return "actions/action_add";
//    }

    @GetMapping("/add")
    public String action(@RequestParam(required = false) Integer matchDay,
                         @RequestParam(required = false) Long playerId,
                         Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());

        ActionFormDTO actionForm = new ActionFormDTO();
        if (matchDay != null) {
            actionForm.setMatchDayNumber(matchDay);
        }
        if (playerId != null) {
            actionForm.setPlayerId(playerId);
        }

        model.addAttribute("actionForm", actionForm);
        model.addAttribute("clubs", clubService.getAllClubs());
        model.addAttribute("players", playerService.getAllPlayers());
        return "actions/action_add";
    }

    // Add new action
    @PostMapping("/add")
    public String addAction(@ModelAttribute ActionFormDTO actionForm,
                            RedirectAttributes redirectAttributes) {
        try {
            actionService.saveAction(actionForm);
            redirectAttributes.addFlashAttribute("success",
                    "Action added successfully for " + actionForm.getPlayerName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error adding action: " + e.getMessage());
        }
        return "redirect:/actions/add";
    }

    // View actions by match day
    @GetMapping("/match-day/{matchDayNumber}")
    public String viewMatchDayActions(@PathVariable Integer matchDayNumber, Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        List<Action> actions = actionService.getActionsByMatchDay(matchDayNumber);

        //sort actions by total points desc
        actions.sort(Comparator.comparingInt(Action::getTotalPoints).reversed());

        model.addAttribute("seasonName", season.getName());
        model.addAttribute("matchDayNumber", matchDayNumber);
        model.addAttribute("actions", actions);

        // Calculate match day totals
        int totalGoals = actions.stream().mapToInt(Action::getGoals).sum();
        int totalAssists = actions.stream().mapToInt(Action::getAssists).sum();
        int totalPoints = actions.stream().mapToInt(Action::getTotalPoints).sum();

        boolean noPointsScored = actions.stream().allMatch(a -> a.getTotalPoints() == 0);

        model.addAttribute("totalGoals", totalGoals);
        model.addAttribute("totalAssists", totalAssists);
        model.addAttribute("totalPoints", totalPoints);
        model.addAttribute("noPointsScored", noPointsScored);

        return "actions/match-day_actions";
    }

    // Edit action form
    @GetMapping("/edit/{actionId}")
    public String editActionForm(@PathVariable Long actionId, Model model) {
        try {
            ActionFormDTO actionForm = actionService.getActionFormForEdit(actionId);
            Season season = seasonService.getCurrentSeason().orElseThrow();

            model.addAttribute("seasonName", season.getName());
            model.addAttribute("actionForm", actionForm);
            model.addAttribute("actionId", actionId);
            model.addAttribute("clubs", clubService.getAllClubs());
            model.addAttribute("players", playerService.getAllPlayers());
            model.addAttribute("editMode", true);

            return "actions/action_add";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Action not found: " + e.getMessage());
            return "redirect:/actions/actions_statistics";
        }
    }

    // Update existing action
    @PostMapping("/update/{actionId}")
    public String updateAction(@PathVariable Long actionId,
                               @ModelAttribute ActionFormDTO actionForm,
                               RedirectAttributes redirectAttributes) {
        try {
            actionService.updateActionFromForm(actionForm);
            redirectAttributes.addFlashAttribute("success",
                    "Action updated successfully for " + actionForm.getPlayerName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error updating action: " + e.getMessage());
        }
        return "redirect:/actions/actions_statistics";
    }

    // Delete action
    @PostMapping("/delete/{actionId}")
    public String deleteAction(@PathVariable Long actionId,
                               RedirectAttributes redirectAttributes) {
        try {
            Action action = actionService.getActionById(actionId);
            String playerName = action.getPlayer().getName();
            Integer matchDay = action.getMatchDay().getNumber();

            actionService.deleteAction(actionId);
            redirectAttributes.addFlashAttribute("success",
                    "Action deleted successfully for " + playerName + " (Match Day " + matchDay + ")");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error deleting action: " + e.getMessage());
        }
        return "redirect:/actions/actions_statistics";
    }

    // View all actions for a player
    @GetMapping("/player/{playerId}")
    public String viewPlayerActions(@PathVariable Long playerId, Model model) {
        try {
            List<Action> actions = actionService.getActionsByPlayer(playerId);
            Season season = seasonService.getCurrentSeason().orElseThrow();

            if (!actions.isEmpty()) {
                model.addAttribute("player", actions.get(0).getPlayer());
            }

            model.addAttribute("seasonName", season.getName());
            model.addAttribute("actions", actions);

            // Calculate player totals
            int totalGoals = actions.stream().mapToInt(Action::getGoals).sum();
            int totalAssists = actions.stream().mapToInt(Action::getAssists).sum();
            int totalPoints = actions.stream().mapToInt(Action::getTotalPoints).sum();

            model.addAttribute("totalGoals", totalGoals);
            model.addAttribute("totalAssists", totalAssists);
            model.addAttribute("totalPoints", totalPoints);

            return "actions/player_action";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading player actions: " + e.getMessage());
            return "redirect:/actions";
        }
    }

    // Management page for actions
    @GetMapping("/manage")
    public String manageActions(@RequestParam(required = false) Integer matchDay, Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());

        if (matchDay != null) {
            List<Action> actions = actionService.getActionsByMatchDay(matchDay);
            model.addAttribute("actions", actions);
            model.addAttribute("selectedMatchDay", matchDay);

            int totalGoals = actions.stream().mapToInt(Action::getGoals).sum();
            int totalAssists = actions.stream().mapToInt(Action::getAssists).sum();
            int totalPoints = actions.stream().mapToInt(Action::getTotalPoints).sum();

            model.addAttribute("totalGoals", totalGoals);
            model.addAttribute("totalAssists", totalAssists);
            model.addAttribute("totalPoints", totalPoints);
            model.addAttribute("actions", actions);
        }

        return "actions/actions_management";
    }



    @GetMapping("/statistics/club/{clubId}")
    public String viewClubStatistics(@PathVariable Long clubId,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size,
                                     @RequestParam(defaultValue = "points") String sortBy,
                                     @RequestParam(defaultValue = "desc") String sortDir,
                                     Model model) {
        return viewActions(page, size, sortBy, sortDir, clubId, model);
    }

    @GetMapping("/streaks")
    public String viewStreaksLeaderboard(@RequestParam(defaultValue = "10") int limit, Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());

        List<StreakLeaderboardDTO> goalStreaks = actionService.getLongestGoalStreaks(limit);
        List<StreakLeaderboardDTO> assistStreaks = actionService.getLongestAssistStreaks(limit);

        model.addAttribute("goalStreaks", goalStreaks);
        model.addAttribute("assistStreaks", assistStreaks);
        model.addAttribute("limit", limit);

        return "actions/action_streaks_leaderboard";
    }

    @GetMapping("/player-streaks/{playerId}")
    @ResponseBody
    public StreakResultDTO getPlayerStreaks(@PathVariable Long playerId) {
        return actionService.calculatePlayerStreaks(playerId);
    }

    @GetMapping("/club/{clubId}")
    @ResponseBody
    public List<PlayerDTO> getPlayersByClub(@PathVariable Long clubId) {
        return playerService.getPlayersByClub(clubId)
                .stream()
                .map(p -> new PlayerDTO(
                        p.getId(),
                        p.getName(),
                        p.getNationality(),
                        p.getFlagPath(),
                        p.getDateOfBirth(),
                        p.getPosition(),
                        p.getShirtNumber(),
                        p.getClub().getId()
                ))
                .toList();
    }


}
