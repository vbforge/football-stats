package com.vbforge.footballstats.controller;

import com.vbforge.footballstats.dto.PlayerDTO;
import com.vbforge.footballstats.dto.PlayerStatisticsDTO;
import com.vbforge.footballstats.entity.Club;
import com.vbforge.footballstats.entity.Player;
import com.vbforge.footballstats.service.FootballStatsService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/players")
public class PlayerController {

    private final FootballStatsService footballStatsService;

    public PlayerController(FootballStatsService footballStatsService) {
        this.footballStatsService = footballStatsService;
    }

    // View player details
    @GetMapping("/{id}")
    public String viewPlayer(@PathVariable Long id, Model model) {
        try {
            Player player = footballStatsService.getPlayerById(id);
            PlayerStatisticsDTO playerStats = footballStatsService.getPlayerDetail(id);

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
            Player player = footballStatsService.getPlayerById(id);
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
            Player existing = footballStatsService.getPlayerById(id);
            player.setClub(existing.getClub());

            footballStatsService.updatePlayer(player);

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
            Player player = footballStatsService.getPlayerById(id);
            Long clubId = player.getClub().getId();
            String playerName = player.getName();

            footballStatsService.deletePlayer(id);
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
}
