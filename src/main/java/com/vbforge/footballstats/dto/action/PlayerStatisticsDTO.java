package com.vbforge.footballstats.dto.action;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerStatisticsDTO {

    /*private Long playerId;
    private String playerName;
    private String position;
    private Integer appearances;
    private String clubName;
    private Integer totalGoals;
    private Integer totalAssists;
    private Integer totalPoints;
    private Integer maxGoalStreak;
    private Integer maxAssistStreak;

    // Constructor for query results
    public PlayerStatisticsDTO(Long playerId, String playerName, String clubName,
                               Long totalGoals, Long totalAssists, Long totalPoints) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.clubName = clubName;
        this.totalGoals = totalGoals.intValue();
        this.totalAssists = totalAssists.intValue();
        this.totalPoints = totalPoints.intValue();
    }*/

    private Long playerId = 0L;
    private String playerName = "Unknown Player";
    private String position = "N/A";
    private Integer appearances = 0;
    private String clubName = "Unknown Club";
    private Integer totalGoals = 0;
    private Integer totalAssists = 0;
    private Integer totalPoints = 0;
    private Integer maxGoalStreak = 0;
    private Integer maxAssistStreak = 0;

    // Constructor for query results
    public PlayerStatisticsDTO(Long playerId, String playerName, String clubName,
                               Long totalGoals, Long totalAssists, Long totalPoints) {
        this.playerId = playerId != null ? playerId : 0L;
        this.playerName = playerName != null ? playerName : "Unknown Player";
        this.clubName = clubName != null ? clubName : "Unknown Club";
        this.totalGoals = totalGoals != null ? totalGoals.intValue() : 0;
        this.totalAssists = totalAssists != null ? totalAssists.intValue() : 0;
        this.totalPoints = totalPoints != null ? totalPoints.intValue() : 0;
    }

}
