package com.vbforge.footballstats.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "match_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_day_id", nullable = false)
    private MatchDay matchDay;

    @Column(nullable = false)
    private Integer points; // 3 for win, 1 for draw, 0 for defeat

    @Column(nullable = false)
    private Integer goalsFor = 0; // Goals scored by this club

    @Column(nullable = false)
    private Integer goalsAgainst = 0; // Goals conceded by this club

    // Calculate goal difference
    public Integer getGoalDifference() {
        return goalsFor - goalsAgainst;
    }

    // Enum for match result type
    public enum ResultType {
        WIN(3), DRAW(1), DEFEAT(0);

        private final int points;

        ResultType(int points) {
            this.points = points;
        }

        public int getPoints() {
            return points;
        }
    }

}
