package com.vbforge.footballstats.dto.action;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionFormDTO {

    private Long playerId;
    private Integer matchDayNumber;
    private Integer goals = 0;
    private Integer assists = 0;

    // Constructor for easier creation
    public ActionFormDTO(Long playerId, Integer matchDayNumber) {
        this.playerId = playerId;
        this.matchDayNumber = matchDayNumber;
        this.goals = 0;
        this.assists = 0;
    }

}
