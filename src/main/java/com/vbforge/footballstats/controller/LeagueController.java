package com.vbforge.footballstats.controller;

import com.vbforge.footballstats.dto.ClubStandingsDTO;
import com.vbforge.footballstats.entity.Season;
import com.vbforge.footballstats.service.SeasonService;
import com.vbforge.footballstats.service.StandingsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/league")
public class LeagueController {

    private final StandingsService standingsService;
    private final SeasonService seasonService;

    public LeagueController(StandingsService standingsService, SeasonService seasonService) {
        this.standingsService = standingsService;
        this.seasonService = seasonService;
    }

    @GetMapping
    public String currentSeasonTable(Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());
        List<ClubStandingsDTO> standings = standingsService.getCurrentSeasonStandings();
        Season currentSeason = seasonService.getCurrentSeasonOrThrow();

        model.addAttribute("standings", standings);
        model.addAttribute("currentSeason", currentSeason);
        model.addAttribute("pageTitle", "League Table - " + currentSeason.getDisplayName());

        return "league";
    }

    @GetMapping("/season/{seasonId}")
    public String seasonTable(@PathVariable Long seasonId, Model model) {
        List<ClubStandingsDTO> standings = standingsService.getStandingsBySeason(seasonId);
        Season season = seasonService.getSeasonById(seasonId)
                .orElseThrow(() -> new IllegalArgumentException("Season not found"));

        model.addAttribute("standings", standings);
        model.addAttribute("selectedSeason", season);
        model.addAttribute("allSeasons", seasonService.getAllSeasons());
        model.addAttribute("pageTitle", "League Table - " + season.getDisplayName());

        return "league";
    }

}
