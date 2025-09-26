package com.vbforge.footballstats.controller;

import com.vbforge.footballstats.dto.action.PlayerStatisticsDTO;
import com.vbforge.footballstats.dto.player.PlayerDTO;
import com.vbforge.footballstats.entity.City;
import com.vbforge.footballstats.entity.Club;
import com.vbforge.footballstats.entity.Player;
import com.vbforge.footballstats.entity.Season;
import com.vbforge.footballstats.service.ClubService;
import com.vbforge.footballstats.service.PlayerService;
import com.vbforge.footballstats.service.SeasonService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
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

    /**
     * Show form to create new player for specific club
     * URL: /players/club/{clubId}/new
     */
    @GetMapping("/club/{clubId}/new")
    public String newPlayerForm(@PathVariable Long clubId, Model model, RedirectAttributes redirectAttributes) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());
        try {
            Club club = clubService.getClubById(clubId);

            // Create empty PlayerDTO for the form
            PlayerDTO playerDTO = new PlayerDTO();
            playerDTO.setClubId(clubId); // Pre-set the club ID

            model.addAttribute("club", club);
            model.addAttribute("player", playerDTO);
            model.addAttribute("isEditMode", false);

            return "players/player_edit";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Club not found");
            return "redirect:/league";
        }
    }

    /**
     * Create new player for specific club
     * URL: POST /players/club/{clubId}
     */
    @PostMapping("/club/{clubId}")
    public String createPlayer(@PathVariable Long clubId,
                               @ModelAttribute("player") PlayerDTO playerDTO,
                               RedirectAttributes redirectAttributes) {

        try {
            // Validate basic form data
            String validationError = validatePlayerForm(playerDTO);
            if (validationError != null) {
                redirectAttributes.addFlashAttribute("errorMessage", validationError);
                redirectAttributes.addFlashAttribute("player", playerDTO);
                return "redirect:/players/club/" + clubId + "/new";
            }

            // Ensure club ID is set correctly
            playerDTO.setClubId(clubId);

            // Validate shirt number uniqueness for this club
            if (playerDTO.getShirtNumber() != null &&
                    playerService.isShirtNumberTaken(clubId, playerDTO.getShirtNumber())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Shirt number " + playerDTO.getShirtNumber() + " is already taken in this club");
                redirectAttributes.addFlashAttribute("player", playerDTO);
                return "redirect:/players/club/" + clubId + "/new";
            }

            // Create the player
            Player newPlayer = playerService.createPlayer(playerDTO);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Player " + newPlayer.getName() + " added successfully to " + newPlayer.getClub().getName());

            return "redirect:/players/club/" + clubId + "/squad/manage";

        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Club not found");
            return "redirect:/league";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error creating player: " + e.getMessage());
            redirectAttributes.addFlashAttribute("player", playerDTO);
            return "redirect:/players/club/" + clubId + "/new";
        }
    }

    // View player details
    @GetMapping("/{id}")
    public String viewPlayer(@PathVariable Long id, Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());
        try {
            Player player = playerService.getPlayerById(id);
            PlayerStatisticsDTO playerStats = playerService.getPlayerDetail(id);

            model.addAttribute("player", player);
            model.addAttribute("playerStats", playerStats);
            return "players/player_detail";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "Player not found");
            return "redirect:/league";
        }
    }


    /**
     * Show form to edit existing player
     * URL: /players/{playerId}/edit
     */
    @GetMapping("/{playerId}/edit")
    public String editPlayerForm(@PathVariable Long playerId, Model model, RedirectAttributes redirectAttributes) {

        try {
            Season season = seasonService.getCurrentSeason().orElseThrow();
            model.addAttribute("seasonName", season.getName());
            Player player = playerService.getPlayerById(playerId);
            PlayerDTO playerDTO = new PlayerDTO(player);

            model.addAttribute("club", player.getClub());
            model.addAttribute("player", playerDTO);
            model.addAttribute("isEditMode", true);

            return "players/player_edit";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Player not found");
            return "redirect:/league";
        }
    }

    /**
     * Update existing player
     * URL: POST /players/{playerId}
     */
    @PostMapping("/{playerId}")
    public String updatePlayer(@PathVariable Long playerId,
                               @ModelAttribute("player") PlayerDTO playerDTO,
                               RedirectAttributes redirectAttributes) {
        try {
            // Validate basic form data
            String validationError = validatePlayerForm(playerDTO);
            if (validationError != null) {
                redirectAttributes.addFlashAttribute("errorMessage", validationError);
                redirectAttributes.addFlashAttribute("player", playerDTO);
                return "redirect:/players/" + playerId + "/edit";
            }

            // Get existing player to get club info
            Player existingPlayer = playerService.getPlayerById(playerId);
            Long clubId = existingPlayer.getClub().getId();

            // Validate shirt number uniqueness (excluding current player)
            if (playerDTO.getShirtNumber() != null &&
                    playerService.isShirtNumberTakenExcluding(clubId, playerDTO.getShirtNumber(), playerId)) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Shirt number " + playerDTO.getShirtNumber() + " is already taken in this club");
                redirectAttributes.addFlashAttribute("player", playerDTO);
                return "redirect:/players/" + playerId + "/edit";
            }

            // Update the player
            playerDTO.setId(playerId);
            playerDTO.setClubId(clubId); // Ensure club doesn't change during edit
            Player updatedPlayer = playerService.updatePlayer(playerDTO);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Player " + updatedPlayer.getName() + " updated successfully");

            return "redirect:/players/club/" + clubId + "/squad/manage";

        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Player not found");
            return "redirect:/league";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating player: " + e.getMessage());
            redirectAttributes.addFlashAttribute("player", playerDTO);
            return "redirect:/players/" + playerId + "/edit";
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
            return "redirect:/players/club/" + clubId + "/squad/manage";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Player not found");
            return "redirect:/league";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error deleting player: " + e.getMessage());
            return "redirect:/league";
        }
    }

    // View All Players in the system (read-only)
    @GetMapping("/all_players")
    public String viewAllPlayers(Model model) {
        List<Player> allPlayers = playerService.getAllPlayers();

        // Group players by position
        Map<Player.Position, List<Player>> playersByPosition = allPlayers.stream()
                .collect(Collectors.groupingBy(Player::getPosition));

        model.addAttribute("players", allPlayers);
        model.addAttribute("goalkeepers", playersByPosition.getOrDefault(Player.Position.GOALKEEPER, List.of()));
        model.addAttribute("defenders", playersByPosition.getOrDefault(Player.Position.DEFENDER, List.of()));
        model.addAttribute("midfielders", playersByPosition.getOrDefault(Player.Position.MIDFIELDER, List.of()));
        model.addAttribute("forwards", playersByPosition.getOrDefault(Player.Position.FORWARD, List.of()));

        return "all_players";
    }

    // View Players by Club squad (read-only)
    @GetMapping("/club/{id}/squad")
    public String viewSquad(@PathVariable Long id, Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());
        try {
            Club club = clubService.getClubById(id);
            List<Player> players = playerService.getPlayersByClub(id);

            // Group players by position
            Map<Player.Position, List<Player>> playersByPosition = players.stream()
                    .collect(Collectors.groupingBy(Player::getPosition));


            model.addAttribute("club", club);
            model.addAttribute("city", club.getCity().getName());
            model.addAttribute("players", players);
            model.addAttribute("goalkeepers", playersByPosition.getOrDefault(Player.Position.GOALKEEPER, List.of()));
            model.addAttribute("defenders", playersByPosition.getOrDefault(Player.Position.DEFENDER, List.of()));
            model.addAttribute("midfielders", playersByPosition.getOrDefault(Player.Position.MIDFIELDER, List.of()));
            model.addAttribute("forwards", playersByPosition.getOrDefault(Player.Position.FORWARD, List.of()));

            return "players/players_squad";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "Club not found");
            return "redirect:/league";
        }
    }

    // Players by Club Squad management (with CRUD)
    @GetMapping("/club/{id}/squad/manage")
    public String manageSquad(@PathVariable Long id, Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());
        try {
            Club club = clubService.getClubById(id);
            List<Player> players = playerService.getPlayersByClub(id);

            // Calculate position counts
            Map<Player.Position, Long> positionCounts = players.stream()
                    .collect(Collectors.groupingBy(Player::getPosition, Collectors.counting()));

            model.addAttribute("club", club);
            model.addAttribute("players", players);
            model.addAttribute("goalkeepersCount", positionCounts.getOrDefault(Player.Position.GOALKEEPER, 0L));
            model.addAttribute("defendersCount", positionCounts.getOrDefault(Player.Position.DEFENDER, 0L));
            model.addAttribute("midfieldersCount", positionCounts.getOrDefault(Player.Position.MIDFIELDER, 0L));
            model.addAttribute("forwardsCount", positionCounts.getOrDefault(Player.Position.FORWARD, 0L));

            return "players/players_squad_manage";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "Club not found");
            return "redirect:/league";
        }
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



    /**
     * Validate player form data
     */
    private String validatePlayerForm(PlayerDTO playerDTO) {
        if (playerDTO.getName() == null || playerDTO.getName().trim().isEmpty()) {
            return "Player name is required";
        }

        if (playerDTO.getName().trim().length() < 2) {
            return "Player name must be at least 2 characters long";
        }

        if (playerDTO.getPosition() == null) {
            return "Player position is required";
        }

        if (playerDTO.getDateOfBirth() == null) {
            return "Date of birth is required";
        }

        if (playerDTO.getDateOfBirth().isAfter(LocalDate.now())) {
            return "Date of birth cannot be in the future";
        }

        // Check minimum age (e.g., 16 years old)
        if (playerDTO.getDateOfBirth().isAfter(LocalDate.now().minusYears(16))) {
            return "Player must be at least 16 years old";
        }

        if (playerDTO.getShirtNumber() != null) {
            if (playerDTO.getShirtNumber() < 1 || playerDTO.getShirtNumber() > 99) {
                return "Shirt number must be between 1 and 99";
            }
        }

        return null; // No validation errors
    }



}
