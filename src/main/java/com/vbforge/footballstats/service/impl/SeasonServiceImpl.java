package com.vbforge.footballstats.service.impl;

import com.vbforge.footballstats.entity.Season;
import com.vbforge.footballstats.repository.SeasonRepository;
import com.vbforge.footballstats.service.SeasonService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository seasonRepository;

    public SeasonServiceImpl(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    public Optional<Season> getCurrentSeason() {
        return seasonRepository.findByIsCurrentTrue();
    }

    public Season getCurrentSeasonOrThrow() {
        return getCurrentSeason()
                .orElseThrow(() -> new IllegalStateException("No current season is set"));
    }

    public List<Season> getAllSeasons() {
        return seasonRepository.findAllByOrderByStartDateDesc();
    }

    public Optional<Season> getSeasonById(Long id) {
        return seasonRepository.findById(id);
    }

    public Optional<Season> getSeasonByName(String name) {
        return seasonRepository.findByName(name);
    }

    @Transactional
    public Season createNewSeason(String name, LocalDate startDate, LocalDate endDate) {
        if (seasonRepository.existsByName(name)) {
            throw new IllegalArgumentException("Season with name '" + name + "' already exists");
        }

        Season season = new Season();
        season.setName(name);
        season.setStartDate(startDate);
        season.setEndDate(endDate);
        season.setCurrent(false); // Don't set as current by default

        return seasonRepository.save(season);
    }

    @Transactional
    public void setCurrentSeason(Long seasonId) {
        // First, set all seasons to not current
        List<Season> allSeasons = seasonRepository.findAll();
        allSeasons.forEach(season -> season.setCurrent(false));
        seasonRepository.saveAll(allSeasons);

        // Then set the selected season as current
        Season newCurrentSeason = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new IllegalArgumentException("Season not found with id: " + seasonId));

        newCurrentSeason.setCurrent(true);
        seasonRepository.save(newCurrentSeason);
    }

    public List<Season> getActiveSeasons() {
        return seasonRepository.findActiveSeasonsToday();
    }

    public boolean isSeasonNameUnique(String name) {
        return !seasonRepository.existsByName(name);
    }
}
