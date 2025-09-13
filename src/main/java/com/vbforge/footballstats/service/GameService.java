package com.vbforge.footballstats.service;

import com.vbforge.footballstats.dto.GameFormDTO;
import com.vbforge.footballstats.entity.Club;
import com.vbforge.footballstats.entity.Game;
import com.vbforge.footballstats.entity.MatchDay;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface GameService {

    // Basic CRUD operations
    List<Game> getAllGames();
    Optional<Game> getGameById(Long id);
    void saveGame(GameFormDTO gameFormDTO);
    Game updateGame(Game game);

    // Calendar functionality
    List<Game> getGamesByClub(Club club);
    List<Game> getGamesByMatchDay(MatchDay matchDay);
    List<Game> getGamesByMonth(YearMonth month);

    // Counters
    long getFinishedGamesCount();
    long getScheduledGamesCount();

    // Get scheduled/finished games
    List<Game> getScheduledGames();
    List<Game> getFinishedGames();

}
