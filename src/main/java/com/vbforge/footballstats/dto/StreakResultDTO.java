package com.vbforge.footballstats.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreakResultDTO {

    private Integer maxGoalStreak;
    private Integer maxAssistStreak;
    private Integer currentGoalStreak;
    private Integer currentAssistStreak;

}
