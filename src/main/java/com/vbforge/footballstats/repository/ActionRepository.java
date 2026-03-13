package com.vbforge.footballstats.repository;

import com.vbforge.footballstats.entity.Action;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActionRepository extends JpaRepository<Action, Long> {

    @Query("SELECT a FROM Action a WHERE a.player.id = :playerId ORDER BY a.matchDay.number")
    List<Action> findByPlayerIdOrderByMatchDay(@Param("playerId") Long playerId);

    @Query("SELECT a FROM Action a WHERE a.player.club.id = :clubId")
    List<Action> findByPlayerClubId(@Param("clubId") Long clubId);

    // Updated query to use weighted points (goals * 3 + assists)
    @Query("SELECT a.player.id, a.player.name, a.player.club.name, " +
            "SUM(a.goals) as totalGoals, SUM(a.assists) as totalAssists, " +
            "SUM(a.goals * 3 + a.assists) as totalPoints " +
            "FROM Action a " +
            "GROUP BY a.player.id, a.player.name, a.player.club.name " +
            "ORDER BY totalPoints DESC")
    List<Object[]> getPlayerStatistics();

    // Updated query with weighted points for club statistics
    @Query("SELECT a.player.id, a.player.name, a.player.club.name, " +
            "SUM(a.goals) as totalGoals, SUM(a.assists) as totalAssists, " +
            "SUM(a.goals * 3 + a.assists) as totalPoints " +
            "FROM Action a " +
            "WHERE a.player.club.id = :clubId " +
            "GROUP BY a.player.id, a.player.name, a.player.club.name " +
            "ORDER BY totalPoints DESC")
    List<Object[]> getPlayerStatisticsByClub(@Param("clubId") Long clubId);

    // Paginated player statistics with weighted points
    @Query("SELECT a.player.id, a.player.name, a.player.club.name, " +
            "SUM(a.goals) as totalGoals, SUM(a.assists) as totalAssists, " +
            "SUM(a.goals * 3 + a.assists) as totalPoints " +
            "FROM Action a " +
            "GROUP BY a.player.id, a.player.name, a.player.club.name " +
            "ORDER BY totalPoints DESC")
    Page<Object[]> getPlayerStatisticsPaginated(Pageable pageable);

    // Paginated club player statistics with weighted points
    @Query("SELECT a.player.id, a.player.name, a.player.club.name, " +
            "SUM(a.goals) as totalGoals, SUM(a.assists) as totalAssists, " +
            "SUM(a.goals * 3 + a.assists) as totalPoints " +
            "FROM Action a " +
            "WHERE a.player.club.id = :clubId " +
            "GROUP BY a.player.id, a.player.name, a.player.club.name " +
            "ORDER BY totalPoints DESC")
    Page<Object[]> getPlayerStatisticsByClubPaginated(@Param("clubId") Long clubId, Pageable pageable);

    // Find action by player and match day for editing
    @Query("SELECT a FROM Action a WHERE a.player.id = :playerId AND a.matchDay.id = :matchDayId")
    Optional<Action> findByPlayerIdAndMatchDayId(@Param("playerId") Long playerId, @Param("matchDayId") Long matchDayId);

    // Find all actions for a specific match day
    @Query("SELECT a FROM Action a WHERE a.matchDay.number = :matchDayNumber ORDER BY a.player.club.name, a.player.name")
    List<Action> findByMatchDayNumber(@Param("matchDayNumber") Integer matchDayNumber);

    // Find longest goal streaks across all players
    @Query(value = "WITH streak_calculation AS (" +
            "  SELECT player_id, match_day_id, goals, " +
            "         CASE WHEN goals > 0 THEN " +
            "           ROW_NUMBER() OVER (PARTITION BY player_id ORDER BY match_day_id) - " +
            "           ROW_NUMBER() OVER (PARTITION BY player_id, goals > 0 ORDER BY match_day_id) " +
            "         END as streak_group " +
            "  FROM actions " +                               //
            "  ORDER BY player_id, match_day_id" +
            "), " +
            "streak_lengths AS (" +
            "  SELECT player_id, COUNT(*) as streak_length " +
            "  FROM streak_calculation " +
            "  WHERE goals > 0 " +
            "  GROUP BY player_id, streak_group" +
            "), " +
            "max_streaks AS (" +
            "  SELECT player_id, MAX(streak_length) as max_streak " +
            "  FROM streak_lengths " +
            "  GROUP BY player_id" +
            ") " +
            "SELECT p.id, p.name, c.name, ms.max_streak " +
            "FROM max_streaks ms " +
            "JOIN players p ON ms.player_id = p.id " +            //
            "JOIN clubs c ON p.club_id = c.id " +                 //
            "ORDER BY ms.max_streak DESC", nativeQuery = true)
    List<Object[]> getLongestGoalStreaks();

    // Find longest assist streaks across all players
    @Query(value = "WITH streak_calculation AS (" +
            "  SELECT player_id, match_day_id, assists, " +
            "         CASE WHEN assists > 0 THEN " +
            "           ROW_NUMBER() OVER (PARTITION BY player_id ORDER BY match_day_id) - " +
            "           ROW_NUMBER() OVER (PARTITION BY player_id, assists > 0 ORDER BY match_day_id) " +
            "         END as streak_group " +
            "  FROM actions " +                                  //
            "  ORDER BY player_id, match_day_id" +
            "), " +
            "streak_lengths AS (" +
            "  SELECT player_id, COUNT(*) as streak_length " +
            "  FROM streak_calculation " +
            "  WHERE assists > 0 " +
            "  GROUP BY player_id, streak_group" +
            "), " +
            "max_streaks AS (" +
            "  SELECT player_id, MAX(streak_length) as max_streak " +
            "  FROM streak_lengths " +
            "  GROUP BY player_id" +
            ") " +
            "SELECT p.id, p.name, c.name, ms.max_streak " +
            "FROM max_streaks ms " +
            "JOIN players p ON ms.player_id = p.id " +                 //
            "JOIN clubs c ON p.club_id = c.id " +                        //
            "ORDER BY ms.max_streak DESC", nativeQuery = true)
    List<Object[]> getLongestAssistStreaks();

    void deleteByPlayerId(Long playerId);
}
