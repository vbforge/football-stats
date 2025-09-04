package com.vbforge.footballstats.repository;

import com.vbforge.footballstats.entity.Club;
import com.vbforge.footballstats.entity.Game;
import com.vbforge.footballstats.entity.MatchDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    // Find all games ordered by date
    List<Game> findAllByOrderByGameDateAsc();

    // Find games by club (home or away)
    @Query("SELECT g FROM Game g WHERE g.homeClub = :club OR g.awayClub = :club ORDER BY g.gameDate ASC")
    List<Game> findByClub(@Param("club") Club club);

    // Find games by match day
    List<Game> findByMatchDayOrderByGameDateAsc(MatchDay matchDay);

    // Find games by status
    List<Game> findByStatusOrderByGameDateAsc(Game.GameStatus status);

    // Find games by date range
    List<Game> findByGameDateBetweenOrderByGameDateAsc(LocalDate startDate, LocalDate endDate);

    // Count finished games
    long countByStatus(Game.GameStatus status);

}
