package com.vbforge.footballstats.service;

import com.vbforge.footballstats.entity.Season;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SeasonService {

    Optional<Season> getCurrentSeason();
    Season getCurrentSeasonOrThrow();
    List<Season> getAllSeasons();
    Optional<Season> getSeasonById(Long id);
    Optional<Season> getSeasonByName(String name);
    Season createNewSeason(String name, LocalDate startDate, LocalDate endDate);
    void setCurrentSeason(Long seasonId);
    List<Season> getActiveSeasons();
    boolean isSeasonNameUnique(String name);

}
