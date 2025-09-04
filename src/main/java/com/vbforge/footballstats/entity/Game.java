package com.vbforge.footballstats.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_club_id", nullable = false)
    private Club homeClub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_club_id", nullable = false)
    private Club awayClub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_day_id", nullable = false)
    private MatchDay matchDay;

    @Column(name = "game_date", nullable = false)
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate gameDate;

    @Column(name = "home_goals")
    private Integer homeGoals;

    @Column(name = "away_goals")
    private Integer awayGoals;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status = GameStatus.SCHEDULED;

    @Column(name = "stadium_name")
    private String stadiumName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    public enum GameStatus {
        SCHEDULED,
        FINISHED
    }

    // Helper methods
    public boolean isFinished() {
        return status == GameStatus.FINISHED;
    }

    public boolean isScheduled() {
        return status == GameStatus.SCHEDULED;
    }

    public String getResult() {
        if (homeGoals == null || awayGoals == null) {
            return "vs";
        }
        return homeGoals + " - " + awayGoals;
    }

    public String getResultForTeam(Club currentTeam) {
        if (homeGoals == null || awayGoals == null) {
            return "vs";
        }

        if (currentTeam.equals(homeClub)) { // if current team played at home
            if (homeGoals > awayGoals) return "win";
            if (homeGoals < awayGoals) return "lose";
            return "draw";
        } else if (currentTeam.equals(awayClub)) { // if current team played away
            if (awayGoals > homeGoals) return "win";
            if (awayGoals < homeGoals) return "lose";
            return "draw";
        }

        return "vs"; // fallback if team not part of this game
    }

}
