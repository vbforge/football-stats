package com.vbforge.footballstats.repository;

import com.vbforge.footballstats.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long>{

    // Find current season
    Optional<Season> findByIsCurrentTrue();

    // Find all seasons ordered by start date desc (newest first)
    List<Season> findAllByOrderByStartDateDesc();

    // Find season by name
    Optional<Season> findByName(String name);

    // Check if season name already exists
    boolean existsByName(String name);

    // Get seasons by active status
    @Query("SELECT s FROM Season s WHERE " +
            "CURRENT_DATE >= s.startDate AND CURRENT_DATE <= s.endDate")
    List<Season> findActiveSeasonsToday();

}
