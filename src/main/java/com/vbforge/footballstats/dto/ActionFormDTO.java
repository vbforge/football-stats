package com.vbforge.footballstats.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionFormDTO {

    private Long playerId;
    private String playerName;
    private String clubName;
    private Integer matchDayNumber;
    private Integer goals = 0;
    private Integer assists = 0;
    private boolean newPlayer = false;

    // Custom getter/setter for boolean field to avoid Lombok naming issues
    public boolean isNewPlayer() {
        return newPlayer;
    }

    public void setNewPlayer(boolean newPlayer) {
        this.newPlayer = newPlayer;
    }

}
