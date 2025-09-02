package com.vbforge.footballstats.repository;

import com.vbforge.footballstats.entity.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionRepository extends JpaRepository<Action, Long> {

    @Query("SELECT a FROM Action a WHERE a.player.id = :playerId ORDER BY a.matchDay.number")
    List<Action> findByPlayerIdOrderByMatchDay(@Param("playerId") Long playerId);

    @Query("SELECT a FROM Action a WHERE a.player.club.id = :clubId")
    List<Action> findByPlayerClubId(@Param("clubId") Long clubId);

    @Query("SELECT a.player.id, a.player.name, a.player.club.name, " +
            "SUM(a.goals) as totalGoals, SUM(a.assists) as totalAssists, " +
            "SUM(a.goals + a.assists) as totalPoints " +
            "FROM Action a " +
            "GROUP BY a.player.id, a.player.name, a.player.club.name " +
            "ORDER BY totalPoints DESC")
    List<Object[]> getPlayerStatistics();

    @Query("SELECT a.player.id, a.player.name, a.player.club.name, " +
            "SUM(a.goals) as totalGoals, SUM(a.assists) as totalAssists, " +
            "SUM(a.goals + a.assists) as totalPoints " +
            "FROM Action a " +
            "WHERE a.player.club.id = :clubId " +
            "GROUP BY a.player.id, a.player.name, a.player.club.name " +
            "ORDER BY totalPoints DESC")
    List<Object[]> getPlayerStatisticsByClub(@Param("clubId") Long clubId);

    void deleteByPlayerId(Long playerId);
}
