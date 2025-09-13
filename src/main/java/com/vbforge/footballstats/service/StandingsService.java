package com.vbforge.footballstats.service;

import com.vbforge.footballstats.dto.ClubStandingsDTO;

import java.util.List;

public interface StandingsService {

    List<ClubStandingsDTO> getCurrentSeasonStandings();
    List<ClubStandingsDTO> getStandingsBySeason(Long seasonId);
    ClubStandingsDTO getClubStandingsForCurrentSeason(Long clubId);
    int getClubPositionInCurrentSeason(Long clubId);




}
