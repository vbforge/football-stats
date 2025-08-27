package com.vbforge.footballstats.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "match_days")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer number;

    @OneToMany(mappedBy = "matchDay", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Action> actions;

}
