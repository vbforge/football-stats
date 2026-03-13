package com.vbforge.footballstats.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "games")
@Getter
@Setter
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
        // Null safety checks
        if (currentTeam == null || homeClub == null || awayClub == null) {
            return "vs";
        }

        if (homeGoals == null || awayGoals == null) {
            return "vs";
        }

        // Check if current team played at home
        if (currentTeam.getId() != null && currentTeam.getId().equals(homeClub.getId())) {
            if (homeGoals > awayGoals) return "win";
            if (homeGoals < awayGoals) return "lose";
            return "draw";
        }
        // Check if current team played away
        else if (currentTeam.getId() != null && currentTeam.getId().equals(awayClub.getId())) {
            if (awayGoals > homeGoals) return "win";
            if (awayGoals < homeGoals) return "lose";
            return "draw";
        }

        return "vs"; // fallback if team not part of this game
    }

    // Alternative method that uses Club name for comparison (less reliable)
    public String getResultForTeamByName(String teamName) {
        if (teamName == null || homeClub == null || awayClub == null) {
            return "vs";
        }

        if (homeGoals == null || awayGoals == null) {
            return "vs";
        }

        if (teamName.equalsIgnoreCase(homeClub.getName())) {
            if (homeGoals > awayGoals) return "win";
            if (homeGoals < awayGoals) return "lose";
            return "draw";
        } else if (teamName.equalsIgnoreCase(awayClub.getName())) {
            if (awayGoals > homeGoals) return "win";
            if (awayGoals < homeGoals) return "lose";
            return "draw";
        }

        return "vs";
    }

}
