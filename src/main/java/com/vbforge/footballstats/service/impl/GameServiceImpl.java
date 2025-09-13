package com.vbforge.footballstats.service.impl;

import com.vbforge.footballstats.dto.GameFormDTO;
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

        Season season = seasonService
                .getSeasonByName(gameFormDTO.getSeasonName())
                .orElseThrow(()->new RuntimeException("Season not found"));

        MatchDay matchDay = matchDayRepository.findByNumberAndSeason(gameFormDTO.getMatchDayNumber(), season)
                .orElseGet(()->{
                    MatchDay newMatchDay = new MatchDay();
                    newMatchDay.setNumber(gameFormDTO.getMatchDayNumber());
                    return matchDayRepository.save(newMatchDay);
                });

        Club homeClub = clubRepository.findByName(gameFormDTO.getHomeClub())
                .orElseThrow(() -> new RuntimeException("Club not found"));
        Club awayClub= clubRepository.findByName(gameFormDTO.getAwayClub())
                .orElseThrow(() -> new RuntimeException("Club not found"));

        Game game = new Game();
        game.setHomeClub(homeClub);
        game.setAwayClub(awayClub);
        game.setMatchDay(matchDay);
        game.setSeason(season);
        game.setGameDate(gameFormDTO.getGameDate());
        game.setHomeGoals(gameFormDTO.getHomeGoals());
        game.setAwayGoals(gameFormDTO.getAwayGoals());
        game.setStatus(Game.GameStatus.valueOf(gameFormDTO.getStatus()));
        game.setStadiumName(homeClub.getStadium());

        gameRepository.save(game);
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
}
