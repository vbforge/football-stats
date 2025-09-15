package com.vbforge.footballstats.service.impl;

import com.vbforge.footballstats.dto.game.GameFormDTO;
import com.vbforge.footballstats.entity.Club;
import com.vbforge.footballstats.entity.Game;
import com.vbforge.footballstats.entity.MatchDay;
import com.vbforge.footballstats.entity.Season;
import com.vbforge.footballstats.repository.ClubRepository;
import com.vbforge.footballstats.repository.GameRepository;
import com.vbforge.footballstats.repository.MatchDayRepository;
import com.vbforge.footballstats.service.GameService;
import com.vbforge.footballstats.service.SeasonService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final MatchDayRepository matchDayRepository;
    private final ClubRepository clubRepository;
    private final SeasonService seasonService;

    public GameServiceImpl(GameRepository gameRepository,
                           MatchDayRepository matchDayRepository,
                           ClubRepository clubRepository,
                           SeasonService seasonService) {
        this.gameRepository = gameRepository;
        this.matchDayRepository = matchDayRepository;
        this.clubRepository = clubRepository;
        this.seasonService = seasonService;
    }

    // Basic CRUD operations
    @Override
    public List<Game> getAllGames() {
        return gameRepository.findAllByOrderByGameDateAsc();
    }

    @Override
    public Optional<Game> getGameById(Long id) {
        return gameRepository.findById(id);
    }

    @Override
    @Transactional
    public void saveGame(GameFormDTO gameFormDTO) {
        // Validate input
        validateGameForm(gameFormDTO);

        Season season = seasonService
                .getSeasonByName(gameFormDTO.getSeasonName())
                .orElseThrow(() -> new RuntimeException("Season not found: " + gameFormDTO.getSeasonName()));

        // Find or create match day
        MatchDay matchDay = matchDayRepository.findByNumberAndSeason(gameFormDTO.getMatchDayNumber(), season)
                .orElseGet(() -> {
                    MatchDay newMatchDay = new MatchDay();
                    newMatchDay.setNumber(gameFormDTO.getMatchDayNumber());
                    newMatchDay.setSeason(season); // FIX: Set the season
                    return matchDayRepository.save(newMatchDay);
                });

        Club homeClub = clubRepository.findByName(gameFormDTO.getHomeClub())
                .orElseThrow(() -> new RuntimeException("Home club not found: " + gameFormDTO.getHomeClub()));

        Club awayClub = clubRepository.findByName(gameFormDTO.getAwayClub())
                .orElseThrow(() -> new RuntimeException("Away club not found: " + gameFormDTO.getAwayClub()));

        // Validate clubs are different
        if (homeClub.getId().equals(awayClub.getId())) {
            throw new IllegalArgumentException("Home and away clubs must be different");
        }

        // Check for duplicate games
        if (isDuplicateGame(homeClub, awayClub, matchDay)) {
            throw new RuntimeException("Game already exists between these clubs in this match day");
        }

        Game game = new Game();
        game.setHomeClub(homeClub);
        game.setAwayClub(awayClub);
        game.setMatchDay(matchDay);
        game.setSeason(season);
        game.setGameDate(gameFormDTO.getGameDate());

        // Handle status and goals properly
        Game.GameStatus status = parseStatus(gameFormDTO.getStatus());
        game.setStatus(status);

        if (status == Game.GameStatus.FINISHED) {
            // Validate goals for finished games
            if (gameFormDTO.getHomeGoals() == null || gameFormDTO.getAwayGoals() == null) {
                throw new IllegalArgumentException("Goals must be provided for finished games");
            }
            if (gameFormDTO.getHomeGoals() < 0 || gameFormDTO.getAwayGoals() < 0) {
                throw new IllegalArgumentException("Goals cannot be negative");
            }
            game.setHomeGoals(gameFormDTO.getHomeGoals());
            game.setAwayGoals(gameFormDTO.getAwayGoals());
        } else {
            // For scheduled games, set goals to null
            game.setHomeGoals(null);
            game.setAwayGoals(null);
        }

        game.setStadiumName(homeClub.getStadium());

        gameRepository.save(game);
    }

    @Override
    @Transactional
    public Game updateGame(Game game) {
        if (game == null || game.getId() == null) {
            throw new IllegalArgumentException("Game and Game ID cannot be null");
        }

        // Verify game exists in database
        if (!gameRepository.existsById(game.getId())) {
            throw new RuntimeException("Game not found with ID: " + game.getId());
        }

        // Additional validation for finished games
        if (game.getStatus() == Game.GameStatus.FINISHED) {
            if (game.getHomeGoals() == null || game.getAwayGoals() == null) {
                throw new IllegalArgumentException("Goals cannot be null for finished games");
            }
            if (game.getHomeGoals() < 0 || game.getAwayGoals() < 0) {
                throw new IllegalArgumentException("Goals cannot be negative");
            }
            if (game.getHomeGoals() > 20 || game.getAwayGoals() > 20) {
                throw new IllegalArgumentException("Goals cannot exceed 20 (unrealistic score)");
            }
        } else {
            // For scheduled games, ensure goals are null
            game.setHomeGoals(null);
            game.setAwayGoals(null);
        }

        return gameRepository.save(game);
    }

    // Calendar functionality
    @Override
    public List<Game> getGamesByClub(Club club) {
        return gameRepository.findByClub(club);
    }

    @Override
    public List<Game> getGamesByMatchDay(MatchDay matchDay) {
        return gameRepository.findByMatchDayOrderByGameDateAsc(matchDay);
    }

    @Override
    public List<Game> getGamesByMonth(YearMonth month) {
        LocalDate startOfMonth = LocalDate.from(month.atDay(1).atStartOfDay());
        LocalDate endOfMonth = LocalDate.from(month.atEndOfMonth().atTime(23, 59, 59));
        return gameRepository.findByGameDateBetweenOrderByGameDateAsc(startOfMonth.atStartOfDay(), endOfMonth.atStartOfDay());
    }

    // Counters
    @Override
    public long getFinishedGamesCount() {
        return gameRepository.countByStatus(Game.GameStatus.FINISHED);
    }

    @Override
    public long getScheduledGamesCount() {
        return gameRepository.countByStatus(Game.GameStatus.SCHEDULED);
    }

    // Get scheduled/finished games
    @Override
    public List<Game> getScheduledGames() {
        return gameRepository.findByStatusOrderByGameDateAsc(Game.GameStatus.SCHEDULED);
    }

    @Override
    public List<Game> getFinishedGames() {
        return gameRepository.findByStatusOrderByGameDateAsc(Game.GameStatus.FINISHED);
    }

    //helper methods for save and update games:
    private void validateGameForm(GameFormDTO gameFormDTO) {
        if (gameFormDTO == null) {
            throw new IllegalArgumentException("Game form cannot be null");
        }
        if (gameFormDTO.getHomeClub() == null || gameFormDTO.getHomeClub().trim().isEmpty()) {
            throw new IllegalArgumentException("Home club must be selected");
        }
        if (gameFormDTO.getAwayClub() == null || gameFormDTO.getAwayClub().trim().isEmpty()) {
            throw new IllegalArgumentException("Away club must be selected");
        }
        if (gameFormDTO.getHomeClub().equals(gameFormDTO.getAwayClub())) {
            throw new IllegalArgumentException("Home and away clubs must be different");
        }
        if (gameFormDTO.getMatchDayNumber() == null || gameFormDTO.getMatchDayNumber() < 1) {
            throw new IllegalArgumentException("Match day number must be positive");
        }
        if (gameFormDTO.getGameDate() == null) {
            throw new IllegalArgumentException("Game date must be provided");
        }
        if (gameFormDTO.getSeasonName() == null || gameFormDTO.getSeasonName().trim().isEmpty()) {
            throw new IllegalArgumentException("Season must be selected");
        }
    }

    private Game.GameStatus parseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return Game.GameStatus.SCHEDULED; // Default to scheduled
        }

        try {
            return Game.GameStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid game status: " + status);
        }
    }

    private boolean isDuplicateGame(Club homeClub, Club awayClub, MatchDay matchDay) {
        // Check if game already exists between these clubs in this match day
        List<Game> existingGames = gameRepository.findByMatchDayOrderByGameDateAsc(matchDay);

        return existingGames.stream().anyMatch(game ->
                (game.getHomeClub().getId().equals(homeClub.getId()) &&
                        game.getAwayClub().getId().equals(awayClub.getId())) ||
                        (game.getHomeClub().getId().equals(awayClub.getId()) &&
                                game.getAwayClub().getId().equals(homeClub.getId()))
        );
    }
}
