package com.vbforge.footballstats.dto.league;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubStandingsDTO {

    private Long clubId;
    private String clubName;
    private String logoPath;
    private Integer matchesPlayed;
    private Integer wins;
    private Integer draws;
    private Integer defeats;
    private Integer totalPoints;
    private Integer goalsFor;
    private Integer goalsAgainst;
    private Integer goalDifference;

    // Constructor for query results
    public ClubStandingsDTO(Long clubId, String clubName, String logoPath, Long matchesPlayed,
                            Long wins, Long draws, Long defeats, Long totalPoints,
                            Long goalsFor, Long goalsAgainst) {
        this.clubId = clubId;
        this.clubName = clubName;
        this.logoPath = logoPath;
        this.matchesPlayed = matchesPlayed.intValue();
        this.wins = wins.intValue();
        this.draws = draws.intValue();
        this.defeats = defeats.intValue();
        this.totalPoints = totalPoints.intValue();
        this.goalsFor = goalsFor.intValue();
        this.goalsAgainst = goalsAgainst.intValue();
        this.goalDifference = this.goalsFor - this.goalsAgainst;
    }

}
