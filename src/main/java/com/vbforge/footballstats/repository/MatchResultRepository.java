package com.vbforge.footballstats.repository;

import com.vbforge.footballstats.dto.ClubStandingsDTO;
import com.vbforge.footballstats.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {

    @Query("SELECT mr FROM MatchResult mr WHERE mr.club.id = :clubId ORDER BY mr.matchDay.number")
    List<MatchResult> findByClubIdOrderByMatchDay(Long clubId);

    @Query("SELECT mr FROM MatchResult mr WHERE mr.matchDay.number = :matchDayNumber")
    List<MatchResult> findByMatchDayNumber(Integer matchDayNumber);

    // Get club standings with all statistics
    @Query("SELECT new com.vbforge.footballstats.dto.ClubStandingsDTO(" +
            "c.id, c.name, " +
            "COUNT(mr), " +
            "SUM(CASE WHEN mr.points = 3 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN mr.points = 1 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN mr.points = 0 THEN 1 ELSE 0 END), " +
            "COALESCE(SUM(mr.points), 0), " +
            "COALESCE(SUM(mr.goalsFor), 0), " +
            "COALESCE(SUM(mr.goalsAgainst), 0)) " +
            "FROM Club c LEFT JOIN MatchResult mr ON c.id = mr.club.id " +
            "GROUP BY c.id, c.name " +
            "ORDER BY COALESCE(SUM(mr.points), 0) DESC, " +
            "COALESCE(SUM(mr.goalsFor) - SUM(mr.goalsAgainst), 0) DESC, " +
            "COALESCE(SUM(mr.goalsFor), 0) DESC")
    List<ClubStandingsDTO> getClubStandings();


}
