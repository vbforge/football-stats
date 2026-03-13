package com.vbforge.footballstats.service.impl;

import com.vbforge.footballstats.dto.action.PlayerStatisticsDTO;
import com.vbforge.footballstats.dto.action.StreakResultDTO;
import com.vbforge.footballstats.dto.player.PlayerDTO;
import com.vbforge.footballstats.entity.Club;
import com.vbforge.footballstats.entity.Player;
import com.vbforge.footballstats.repository.ActionRepository;
import com.vbforge.footballstats.repository.ClubRepository;
import com.vbforge.footballstats.repository.PlayerRepository;
import com.vbforge.footballstats.service.ActionService;
import com.vbforge.footballstats.service.PlayerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final ActionRepository actionRepository;
    private final ClubRepository clubRepository;
    private final ActionService actionService;

    public PlayerServiceImpl(PlayerRepository playerRepository,
                             ActionRepository actionRepository,
                             ClubRepository clubRepository, ActionService actionService) {
        this.playerRepository = playerRepository;
        this.actionRepository = actionRepository;
        this.clubRepository = clubRepository;
        this.actionService = actionService;
    }


    @Override
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @Override
    public List<Player> getPlayersByClub(Long clubId) {
        return playerRepository.findByClub_Id(clubId);
    }

    @Override
    @Transactional(readOnly = true)
    public Player getPlayerById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Player not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isShirtNumberTaken(Long clubId, Integer shirtNumber) {
        if (shirtNumber == null) {
            return false;
        }
        return playerRepository.existsByClubIdAndShirtNumber(clubId, shirtNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isShirtNumberTakenExcluding(Long clubId, Integer shirtNumber, Long excludePlayerId) {
        if (shirtNumber == null) {
            return false;
        }
        return playerRepository.existsByClubIdAndShirtNumberAndIdNot(clubId, shirtNumber, excludePlayerId);
    }

    @Override
    @Transactional
    public Player updatePlayer(PlayerDTO playerDTO) {
        if (playerDTO.getId() == null) {
            throw new IllegalArgumentException("Player ID is required for update");
        }

        // Validate input
        validatePlayerDTO(playerDTO);

        // Get existing player
        Player existingPlayer = playerRepository.findById(playerDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Player not found with id: " + playerDTO.getId()));

        // Check shirt number uniqueness if changed
        if (playerDTO.getShirtNumber() != null &&
                !Objects.equals(existingPlayer.getShirtNumber(), playerDTO.getShirtNumber()) &&
                isShirtNumberTaken(existingPlayer.getClub().getId(), playerDTO.getShirtNumber())) {
            throw new IllegalArgumentException("Shirt number " + playerDTO.getShirtNumber() + " is already taken in this club");
        }

        // Update player data
        existingPlayer.setName(playerDTO.getName().trim());
        existingPlayer.setNationality(playerDTO.getNationality() != null ? playerDTO.getNationality().trim() : null);
        existingPlayer.setFlagPath(playerDTO.getFlagPath());
        existingPlayer.setDateOfBirth(playerDTO.getDateOfBirth());
        existingPlayer.setPosition(playerDTO.getPosition());
        existingPlayer.setShirtNumber(playerDTO.getShirtNumber());
        // Note: Club is not updated to prevent accidental transfers

        return playerRepository.save(existingPlayer);
    }

    @Override
    public void deletePlayer(Long playerId) {
        if (playerRepository.existsById(playerId)) {
            // First delete all actions related to this player
            actionRepository.deleteByPlayerId(playerId);
            // Then delete the player
            playerRepository.deleteById(playerId);
        } else {
            throw new EntityNotFoundException("Player not found with id: " + playerId);
        }
    }

    @Override
    @Transactional
    public void savePlayer(Player player) {
        playerRepository.save(player);
    }

    @Override
    @Transactional
    public Player createPlayer(PlayerDTO playerDTO) {
        // Validate input
        validatePlayerDTO(playerDTO);

        // Get club
        Club club = clubRepository.findById(playerDTO.getClubId())
                .orElseThrow(() -> new EntityNotFoundException("Club not found with id: " + playerDTO.getClubId()));

        // Check shirt number uniqueness if provided
        if (playerDTO.getShirtNumber() != null &&
                isShirtNumberTaken(playerDTO.getClubId(), playerDTO.getShirtNumber())) {
            throw new IllegalArgumentException("Shirt number " + playerDTO.getShirtNumber() + " is already taken in this club");
        }

        // Create new player
        Player player = new Player();
        player.setName(playerDTO.getName().trim());
        player.setNationality(playerDTO.getNationality() != null ? playerDTO.getNationality().trim() : null);
        player.setFlagPath(playerDTO.getFlagPath());
        player.setDateOfBirth(playerDTO.getDateOfBirth());
        player.setPosition(playerDTO.getPosition());
        player.setShirtNumber(playerDTO.getShirtNumber());
        player.setClub(club);

        return playerRepository.save(player);
    }

    @Override
    public PlayerStatisticsDTO getPlayerDetail(Long playerId) {
        List<PlayerStatisticsDTO> allPlayers = getAllPlayerStatistics();
        return allPlayers.stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Player not found with id: " + playerId));
    }

    @Override
    public List<PlayerStatisticsDTO> getAllPlayerStatistics() {
        List<Object[]> results = actionRepository.getPlayerStatistics();
        return results.stream()
                .map(result -> {
                    PlayerStatisticsDTO dto = new PlayerStatisticsDTO(
                            (Long) result[0],      // playerId
                            (String) result[1],    // playerName
                            (String) result[2],    // clubName
                            (Long) result[3],      // totalGoals
                            (Long) result[4],      // totalAssists
                            (Long) result[5]       // totalPoints (now weighted)
                    );
                    // Calculate streaks
                    StreakResultDTO streaks = actionService.calculatePlayerStreaks(dto.getPlayerId());
                    dto.setMaxGoalStreak(streaks.getMaxGoalStreak());
                    dto.setMaxAssistStreak(streaks.getMaxAssistStreak());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Validate PlayerDTO data
     */
    private void validatePlayerDTO(PlayerDTO playerDTO) {
        if (playerDTO == null) {
            throw new IllegalArgumentException("Player data cannot be null");
        }

        if (playerDTO.getName() == null || playerDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Player name is required");
        }

        if (playerDTO.getName().trim().length() < 2) {
            throw new IllegalArgumentException("Player name must be at least 2 characters long");
        }

        if (playerDTO.getPosition() == null) {
            throw new IllegalArgumentException("Player position is required");
        }

        if (playerDTO.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Date of birth is required");
        }

        if (playerDTO.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }

        // Check minimum age (16 years old)
        if (playerDTO.getDateOfBirth().isAfter(LocalDate.now().minusYears(16))) {
            throw new IllegalArgumentException("Player must be at least 16 years old");
        }

        // Check maximum age (50 years old)
        if (playerDTO.getDateOfBirth().isBefore(LocalDate.now().minusYears(50))) {
            throw new IllegalArgumentException("Player cannot be older than 50 years");
        }

        if (playerDTO.getShirtNumber() != null) {
            if (playerDTO.getShirtNumber() < 1 || playerDTO.getShirtNumber() > 99) {
                throw new IllegalArgumentException("Shirt number must be between 1 and 99");
            }
        }

        if (playerDTO.getClubId() == null) {
            throw new IllegalArgumentException("Club ID is required");
        }
    }




}