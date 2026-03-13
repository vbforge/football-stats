package com.vbforge.footballstats.dto.player;

import com.vbforge.footballstats.entity.Player;
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
    private String flagPath;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateOfBirth;
    private Player.Position position;
    private Integer shirtNumber;
    private Long clubId;

    public PlayerDTO(Player player) {
        this.id = player.getId();
        this.name = player.getName();
        this.nationality = player.getNationality();
        this.flagPath = player.getFlagPath();
        this.dateOfBirth = player.getDateOfBirth();
        this.position = player.getPosition();
        this.shirtNumber = player.getShirtNumber();
        this.clubId = player.getClub() != null ? player.getClub().getId() : null;
    }
}
