package com.vbforge.footballstats.dto.action;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreakLeaderboardDTO {

    private Long playerId;
    private String playerName;
    private String clubName;
    private Number streakLength;
    private String streakType; // "GOALS" or "ASSISTS"

    // Constructor for query results
    public StreakLeaderboardDTO(Long playerId, String playerName, String clubName, Integer streakLength) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.clubName = clubName;
        this.streakLength = streakLength;
    }
}
