package com.vbforge.footballstats.dto.game;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameFormDTO {

    private Long gameId;
    @NotBlank(message = "Home club must be selected")
    private String homeClub;
    @NotBlank(message = "Away club must be selected")
    private String awayClub;
    @NotNull(message = "Match day is required")
    @Min(value = 1, message = "Match day must be positive")
    private Integer matchDayNumber;
    private String seasonName;
    private LocalDate gameDate;
    private Integer homeGoals;
    private Integer awayGoals;
    private String status;
    private String stadiumName;

}
