package com.vbforge.footballstats.dto.action;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerStatisticsDTO {

    private Long playerId;
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
    }

}
