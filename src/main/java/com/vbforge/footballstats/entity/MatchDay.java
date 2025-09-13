package com.vbforge.footballstats.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "match_days")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer number;

    @OneToMany(mappedBy = "matchDay", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Action> actions;

    @OneToMany(mappedBy = "matchDay", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Game> games;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

}
