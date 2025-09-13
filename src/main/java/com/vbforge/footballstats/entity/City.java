package com.vbforge.footballstats.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "cities")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "x_coordinate")
    private int x; // X coordinate

    @Column(name = "y_coordinate")
    private int y; // Y coordinate

    @Column
    private int population;

    @Column(columnDefinition = "TEXT")
    private String description;

    // One city can have many clubs
    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Club> clubs;

    // Business logic methods
    public String getFormattedPopulation() {
        if (population >= 1000000) {
            return String.format("%.1fM", population / 1000000.0);
        } else if (population >= 1000) {
            return String.format("%.0fK", population / 1000.0);
        }
        return String.valueOf(population);
    }

    public int getPointRadius() {
        // Vary point size based on population or importance
        if (population > 1000000) {
            return 10;
        } else if (population > 500000) {
            return 8;
        } else if (population > 100000) {
            return 6;
        }
        return 4;
    }
}