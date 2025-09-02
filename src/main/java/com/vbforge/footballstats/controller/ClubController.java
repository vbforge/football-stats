package com.vbforge.footballstats.controller;

import com.vbforge.footballstats.dto.ClubDetailDTO;
import com.vbforge.footballstats.entity.Club;
import com.vbforge.footballstats.entity.Player;
import com.vbforge.footballstats.service.FootballStatsService;
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
@RequestMapping("/clubs")
public class ClubController {

    private final FootballStatsService footballStatsService;

    public ClubController(FootballStatsService footballStatsService) {
        this.footballStatsService = footballStatsService;
    }

    // Club details
    @GetMapping("/{id}")
    public String viewClub(@PathVariable Long id, Model model) {
        try {
            ClubDetailDTO club = footballStatsService.getClubDetail(id);
            model.addAttribute("club", club);
            return "club-detail";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "Club not found");
            return "redirect:/league";
        }
    }

    // Edit club form
    @GetMapping("/{id}/edit")
    public String editClubForm(@PathVariable Long id, Model model) {
        try {
            Club club = footballStatsService.getClubById(id);
            model.addAttribute("club", club);
            return "club-edit";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "Club not found");
            return "redirect:/league";
        }
    }

    // Update club
    @PostMapping("/{id}/update")
    public String updateClub(@PathVariable Long id,
                             @ModelAttribute Club club,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please fix the errors in the form");
            return "redirect:/clubs/" + id + "/edit";
        }

        try {
            club.setId(id);
            footballStatsService.updateClub(club);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Club " + club.getName() + " updated successfully");
            return "redirect:/clubs/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating club: " + e.getMessage());
            return "redirect:/clubs/" + id + "/edit";
        }
    }

    // View squad (read-only)
    @GetMapping("/{id}/squad")
    public String viewSquad(@PathVariable Long id, Model model) {
        try {
            Club club = footballStatsService.getClubById(id);
            List<Player> players = footballStatsService.getPlayersByClub(id);

            // Group players by position
            Map<Player.Position, List<Player>> playersByPosition = players.stream()
                    .collect(Collectors.groupingBy(Player::getPosition));

            model.addAttribute("club", club);
            model.addAttribute("players", players);
            model.addAttribute("goalkeepers", playersByPosition.getOrDefault(Player.Position.GOALKEEPER, List.of()));
            model.addAttribute("defenders", playersByPosition.getOrDefault(Player.Position.DEFENDER, List.of()));
            model.addAttribute("midfielders", playersByPosition.getOrDefault(Player.Position.MIDFIELDER, List.of()));
            model.addAttribute("forwards", playersByPosition.getOrDefault(Player.Position.FORWARD, List.of()));

            return "squad";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "Club not found");
            return "redirect:/league";
        }
    }

    // Squad management (with CRUD)
    @GetMapping("/{id}/squad/manage")
    public String manageSquad(@PathVariable Long id, Model model) {
        try {
            Club club = footballStatsService.getClubById(id);
            List<Player> players = footballStatsService.getPlayersByClub(id);

            // Calculate position counts
            Map<Player.Position, Long> positionCounts = players.stream()
                    .collect(Collectors.groupingBy(Player::getPosition, Collectors.counting()));

            model.addAttribute("club", club);
            model.addAttribute("players", players);
            model.addAttribute("goalkeepersCount", positionCounts.getOrDefault(Player.Position.GOALKEEPER, 0L));
            model.addAttribute("defendersCount", positionCounts.getOrDefault(Player.Position.DEFENDER, 0L));
            model.addAttribute("midfieldersCount", positionCounts.getOrDefault(Player.Position.MIDFIELDER, 0L));
            model.addAttribute("forwardsCount", positionCounts.getOrDefault(Player.Position.FORWARD, 0L));

            return "squad-management";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "Club not found");
            return "redirect:/league";
        }
    }

    // New player form
    @GetMapping("/{id}/players/new")
    public String newPlayerForm(@PathVariable Long id, Model model) {
        try {
            Club club = footballStatsService.getClubById(id);
            Player player = new Player();
            player.setClub(club);

            model.addAttribute("club", club);
            model.addAttribute("player", player);
            return "player-edit";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "Club not found");
            return "redirect:/league";
        }
    }

    // Create new player
    @PostMapping("/{id}/players")
    public String createPlayer(@PathVariable Long id,
                               @ModelAttribute Player player,
                               @RequestParam Long clubId,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please fix the errors in the form");
            return "redirect:/clubs/" + id + "/players/new";
        }

        try {
            Club club = footballStatsService.getClubById(clubId);
            player.setClub(club);
            footballStatsService.savePlayer(player);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Player " + player.getName() + " added successfully to " + club.getName());
            return "redirect:/clubs/" + id + "/squad/manage";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error creating player: " + e.getMessage());
            return "redirect:/clubs/" + id + "/players/new";
        }
    }
}