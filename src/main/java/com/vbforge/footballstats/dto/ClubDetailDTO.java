package com.vbforge.footballstats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubDetailDTO {

    private Long id;
    private String name;
    private String coach;
    private String logoPath;
    private String city;
    private Integer foundedYear;
    private String stadium;
    private Integer stadiumCapacity;
    private String description;
    private String website;
    private String nickname;
    private String stadiumImagePath;
    private String primaryColor;
    private String secondaryColor;

    // Statistics
    private Integer totalPlayers;
    private Integer totalGoals;
    private Integer totalAssists;
    private Integer matchesPlayed;
    private Integer wins;
    private Integer draws;
    private Integer defeats;
    private Integer totalPoints;
    private Integer goalsFor;
    private Integer goalsAgainst;
    private Integer goalDifference;
    private Integer currentPosition;

    // Top players
    private List<PlayerStatsDTO> topScorers;
    private List<PlayerStatsDTO> topAssisters;

}
