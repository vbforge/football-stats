package com.vbforge.footballstats.repository;

import com.vbforge.footballstats.entity.MatchDay;
import com.vbforge.footballstats.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchDayRepository extends JpaRepository<MatchDay, Long> {

    // Find match day by number and season
    Optional<MatchDay> findByNumberAndSeason(Integer number, Season season);

    // Find all match days for a season
    List<MatchDay> findBySeasonOrderByNumberAsc(Season season);

    // Find match day by number for current season
    @Query("SELECT md FROM MatchDay md WHERE md.number = :number AND md.season.isCurrent = true")
    Optional<MatchDay> findByNumberInCurrentSeason(@Param("number") Integer number);

    // Get match day by ID (keeping for backward compatibility)
    @Query("SELECT md FROM MatchDay md WHERE md.id = :id")
    Optional<MatchDay> getMatchDayById(@Param("id") Long id);
}
