package com.vbforge.footballstats.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_day_id", nullable = false)
    private MatchDay matchDay;

    @Column(nullable = false)
    private Integer goals = 0;

    @Column(nullable = false)
    private Integer assists = 0;

    //probably better to have this functionality in the service
    //calculate total points (1 point for goal, 1 point for assist)
    public Integer getTotalPoints(){
        return goals + assists;
    }

}
