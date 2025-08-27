package com.vbforge.footballstats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultFormDTO {

    private Long clubId;
    private String clubName;
    private Integer matchDayNumber;
    private Integer points; // 3, 1, or 0
    private Integer goalsFor = 0;
    private Integer goalsAgainst = 0;
    private String resultType; // "WIN", "DRAW", "DEFEAT"
    private boolean isNewClub = false;

}
