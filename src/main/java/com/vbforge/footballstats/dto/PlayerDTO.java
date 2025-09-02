package com.vbforge.footballstats.dto;

import com.vbforge.footballstats.entity.Player;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDTO {

    private Long id;
    private String name;
    private String nationality;
    private String flagPath;//wanted to have a nationality flag svg reference
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateOfBirth; // example: 24/03/2001
    private Player.Position position;
    private Integer shirtNumber;

}
