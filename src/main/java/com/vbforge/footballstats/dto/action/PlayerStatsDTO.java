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
        /*this.id = playerStats.getPlayerId();
        this.name = playerStats.getPlayerName();
        this.position = playerStats.getPosition();
        this.goals = playerStats.getTotalGoals();
        this.assists = playerStats.getTotalAssists();
        this.appearances = playerStats.getAppearances();*/
        /*if (playerStats != null) {
            this.id = playerStats.getPlayerId() != null ? playerStats.getPlayerId() : 0L;
            this.name = playerStats.getPlayerName() != null ? playerStats.getPlayerName() : "Unknown Player";
            this.position = playerStats.getPosition() != null ? playerStats.getPosition() : "N/A";
            this.goals = playerStats.getTotalGoals() != null ? playerStats.getTotalGoals() : 0;
            this.assists = playerStats.getTotalAssists() != null ? playerStats.getTotalAssists() : 0;
            this.appearances = playerStats.getAppearances() != null ? playerStats.getAppearances() : 0;
        }*/

        if (playerStats != null) {
            this.id = playerStats.getPlayerId();
            this.name = playerStats.getPlayerName();
            this.position = playerStats.getPosition();
            this.goals = playerStats.getTotalGoals();
            this.assists = playerStats.getTotalAssists();
            this.appearances = playerStats.getAppearances();
        }

    }

}
