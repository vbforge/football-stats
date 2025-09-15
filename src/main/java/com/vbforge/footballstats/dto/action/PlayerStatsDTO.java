package com.vbforge.footballstats.dto.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStatsDTO {

    private Long id;
    private String name;
    private String position;
    private Integer goals;
    private Integer assists;
    private Integer appearances;

    // Constructor to convert from PlayerStatisticsDTO
    public PlayerStatsDTO(PlayerStatisticsDTO playerStats) {
        this.id = playerStats.getPlayerId();
        this.name = playerStats.getPlayerName();
        this.position = playerStats.getPosition();
        this.goals = playerStats.getTotalGoals();
        this.assists = playerStats.getTotalAssists();
        this.appearances = playerStats.getAppearances();
    }

}
