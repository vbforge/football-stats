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

    // FIXED: Updated route mapping to match HTML links
    @GetMapping({"/statistics", ""})
    public String viewActions(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
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

        // FIXED: Calculate totals from ALL statistics, not just current page
        List<PlayerStatisticsDTO> allStats;
        if (clubId != null) {
            allStats = actionService.getPlayerStatisticsByClub(clubId);
        } else {
            allStats = actionService.getAllPlayerStatistics();
        }

        int totalGoals = allStats.stream().mapToInt(PlayerStatisticsDTO::getTotalGoals).sum();
        int totalAssists = allStats.stream().mapToInt(PlayerStatisticsDTO::getTotalAssists).sum();
        int totalPoints = allStats.stream().mapToInt(PlayerStatisticsDTO::getTotalPoints).sum();

        model.addAttribute("totalGoals", totalGoals);
        model.addAttribute("totalAssists", totalAssists);
        model.addAttribute("totalPoints", totalPoints);
        model.addAttribute("activePlayers", statsPage.getTotalElements());

        // Top performers from current page (for performance display)
        List<PlayerStatisticsDTO> pageStats = statsPage.getContent();
        List<PlayerStatisticsDTO> topScorers = pageStats.stream()
                .filter(stat -> stat.getTotalGoals() > 0)
                .sorted((a, b) -> b.getTotalGoals().compareTo(a.getTotalGoals()))
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("topScorers", topScorers);

        List<PlayerStatisticsDTO> topAssistProviders = pageStats.stream()
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

    @GetMapping("/add")
    public String action(@RequestParam(required = false) Integer matchDay,
                         @RequestParam(required = false) Long playerId,
                         Model model) {

        model.addAttribute("editMode", Boolean.FALSE);

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
                    "Action added successfully for player ID: " + actionForm.getPlayerId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error adding action: " + e.getMessage());
        }
        return "redirect:/actions/add";
    }

    // FIXED: Match day navigation with 1-38 limit
    @GetMapping("/match-day/{matchDayNumber}")
    public String viewMatchDayActions(@PathVariable Integer matchDayNumber, Model model) {
        // Validate match day number (1-38 only)
        if (matchDayNumber < 1 || matchDayNumber > 38) {
            model.addAttribute("errorMessage", "Match day must be between 1 and 38");
            matchDayNumber = Math.max(1, Math.min(38, matchDayNumber)); // Clamp to valid range
        }

        Season season = seasonService.getCurrentSeason().orElseThrow();
        List<Action> actions = actionService.getActionsByMatchDay(matchDayNumber);

        //sort actions by total points desc
        actions.sort(Comparator.comparingInt(Action::getTotalPoints).reversed());

        model.addAttribute("seasonName", season.getName());
        model.addAttribute("matchDayNumber", matchDayNumber);
        model.addAttribute("actions", actions);

        // Add navigation flags
        model.addAttribute("hasPrevious", matchDayNumber > 1);
        model.addAttribute("hasNext", matchDayNumber < 38);
        model.addAttribute("previousMatchDay", Math.max(1, matchDayNumber - 1));
        model.addAttribute("nextMatchDay", Math.min(38, matchDayNumber + 1));

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
            Action action = actionService.getActionById(actionId);
            Season season = seasonService.getCurrentSeason().orElseThrow();

            ActionFormDTO actionForm = new ActionFormDTO();
            actionForm.setPlayerId(action.getPlayer().getId());
            actionForm.setMatchDayNumber(action.getMatchDay().getNumber());
            actionForm.setGoals(action.getGoals());
            actionForm.setAssists(action.getAssists());

            model.addAttribute("editMode", Boolean.FALSE);
            model.addAttribute("seasonName", season.getName());
            model.addAttribute("actionForm", actionForm);
            model.addAttribute("actionId", actionId);
            model.addAttribute("clubs", clubService.getAllClubs());
            model.addAttribute("players", playerService.getAllPlayers());
            model.addAttribute("editMode", true);
            model.addAttribute("selectedPlayer", action.getPlayer());

            return "actions/action_add";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Action not found: " + e.getMessage());
            return "redirect:/actions";
        }
    }

    // Update existing action
    @PostMapping("/update/{actionId}")
    public String updateAction(@PathVariable Long actionId,
                               @ModelAttribute ActionFormDTO actionForm,
                               RedirectAttributes redirectAttributes) {
        try {
            actionService.updateAction(actionId, actionForm);
            redirectAttributes.addFlashAttribute("success",
                    "Action updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error updating action: " + e.getMessage());
        }
        return "redirect:/actions";
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
        return "redirect:/actions";
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
    public String viewStreaksLeaderboard(@RequestParam(defaultValue = "5") int limit, Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());

        List<StreakLeaderboardDTO> goalStreaks = actionService.getLongestGoalStreaks(limit);
        List<StreakLeaderboardDTO> assistStreaks = actionService.getLongestAssistStreaks(limit);

        // FIXED: Add combined streaks - using reflection to call the method
        List<StreakLeaderboardDTO> combinedStreaks;
        try {
            java.lang.reflect.Method method = actionService.getClass().getMethod("getLongestCombinedStreaks", int.class);
            combinedStreaks = (List<StreakLeaderboardDTO>) method.invoke(actionService, limit);
        } catch (Exception e) {
            combinedStreaks = new java.util.ArrayList<>(); // Fallback to empty list
        }

        model.addAttribute("goalStreaks", goalStreaks);
        model.addAttribute("assistStreaks", assistStreaks);
        model.addAttribute("combinedStreaks", combinedStreaks);
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
