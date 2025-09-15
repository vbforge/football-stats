package com.vbforge.footballstats.dto.game;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GameUpdateDTO {

    private Long gameId;
    private Integer homeGoals;
    private Integer awayGoals;
    private String status;

    // Constructor from existing game
    public GameUpdateDTO(Long gameId, Integer homeGoals, Integer awayGoals, String status) {
        this.gameId = gameId;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.status = status;
    }

    // Validation methods
    public boolean isValid() {
        return homeGoals != null && awayGoals != null &&
                homeGoals >= 0 && awayGoals >= 0 &&
                homeGoals <= 20 && awayGoals <= 20 &&
                status != null && !status.trim().isEmpty();
    }

    public boolean isFinished() {
        return "FINISHED".equals(status);
    }

}
