package com.vbforge.footballstats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameFormDTO {

    private Long gameId;
    private String homeClub;
    private String awayClub;
    private Integer matchDayNumber;
    private LocalDate gameDate;
    private Integer homeGoals;
    private Integer awayGoals;
    private String status;
    private String stadiumName;

}
