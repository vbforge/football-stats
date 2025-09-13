package com.vbforge.footballstats.controller;

import com.vbforge.footballstats.dto.PlayerDTO;
import com.vbforge.footballstats.dto.PlayerStatisticsDTO;
import com.vbforge.footballstats.dto.StreakResultDTO;
import com.vbforge.footballstats.entity.Club;
import com.vbforge.footballstats.entity.Player;
import com.vbforge.footballstats.entity.Season;
import com.vbforge.footballstats.service.ClubService;
import com.vbforge.footballstats.service.PlayerService;
import com.vbforge.footballstats.service.SeasonService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;
    private final ClubService clubService;
    private final SeasonService seasonService;

    public PlayerController(PlayerService playerService,
                            ClubService clubService,
                            SeasonService seasonService) {
        this.playerService = playerService;
        this.clubService = clubService;
        this.seasonService = seasonService;
    }

    // View player details
    @GetMapping("/{id}")
    public String viewPlayer(@PathVariable Long id, Model model) {
        try {
            Player player = playerService.getPlayerById(id);
            PlayerStatisticsDTO playerStats = playerService.getPlayerDetail(id);

            model.addAttribute("player", player);
            model.addAttribute("playerStats", playerStats);
            return "player-detail";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "Player not found");
            return "redirect:/league";
        }
    }

    // Edit player form
    @GetMapping("/{id}/edit")
    public String editPlayerForm(@PathVariable Long id, Model model) {
        try {
            Player player = playerService.getPlayerById(id);
            Club club = player.getClub();

            model.addAttribute("player", player);
            model.addAttribute("club", club);
            return "player-edit";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "Player not found");
            return "redirect:/league";
        }
    }

    @PostMapping("/{id}/update")
    public String updatePlayer(@PathVariable Long id,
                               @ModelAttribute Player player,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please fix the errors in the form");
            return "redirect:/players/" + id + "/edit";
        }

        try {
            player.setId(id);

            // Ensure club is not null
            Player existing = playerService.getPlayerById(id);
            player.setClub(existing.getClub());

            playerService.updatePlayer(player);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Player " + player.getName() + " updated successfully");
            return "redirect:/players/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating player: " + e.getMessage());
            return "redirect:/players/" + id + "/edit";
        }
    }

    // Delete player
    @PostMapping("/{id}/delete")
    public String deletePlayer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Player player = playerService.getPlayerById(id);
            Long clubId = player.getClub().getId();
            String playerName = player.getName();

            playerService.deletePlayer(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Player " + playerName + " deleted successfully");
            return "redirect:/clubs/" + clubId + "/squad/manage";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Player not found");
            return "redirect:/league";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error deleting player: " + e.getMessage());
            return "redirect:/league";
        }
    }

    @GetMapping("/statistics")
    public String viewStatistics(Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());
        List<PlayerStatisticsDTO> stats = playerService.getAllPlayerStatistics();
        model.addAttribute("playerStats", stats);
        model.addAttribute("clubs", clubService.getAllClubs());

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

        return "players_statistics";
    }

    @GetMapping("/statistics/club/{clubId}")
    public String viewClubStatistics(@PathVariable Long clubId, Model model) {
        List<PlayerStatisticsDTO> stats = playerService.getPlayerStatisticsByClub(clubId);
        model.addAttribute("playerStats", stats);
        model.addAttribute("clubs", clubService.getAllClubs());
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

        return "players_statistics";
    }

    @GetMapping("/player-streaks/{playerId}")
    @ResponseBody
    public StreakResultDTO getPlayerStreaks(@PathVariable Long playerId) {
        return playerService.calculatePlayerStreaks(playerId);
    }

    @GetMapping("/api/players/club/{clubId}")
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
                        p.getShirtNumber()
                ))
                .toList();
    }

    @GetMapping("/squad")
    public String viewAllSquads(Model model) {
        List<Player> allPlayers = playerService.getAllPlayers();

        // Group players by position
        Map<Player.Position, List<Player>> playersByPosition = allPlayers.stream()
                .collect(Collectors.groupingBy(Player::getPosition));

        model.addAttribute("players", allPlayers);
        model.addAttribute("goalkeepers", playersByPosition.getOrDefault(Player.Position.GOALKEEPER, List.of()));
        model.addAttribute("defenders", playersByPosition.getOrDefault(Player.Position.DEFENDER, List.of()));
        model.addAttribute("midfielders", playersByPosition.getOrDefault(Player.Position.MIDFIELDER, List.of()));
        model.addAttribute("forwards", playersByPosition.getOrDefault(Player.Position.FORWARD, List.of()));

        return "squad";
    }

}
