package com.vbforge.footballstats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityDTO {

    private Long id;
    private String name;
    private int x;
    private int y;
    private int population;
    private String description;
    private String formattedPopulation;
    private int pointRadius;
    private int clubCount;
    private List<String> clubNames;

    // Constructor for basic city info
    public CityDTO(Long id, String name, int x, int y, int population, String description) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.population = population;
        this.description = description;
        this.formattedPopulation = formatPopulation(population);
        this.pointRadius = calculatePointRadius(population);
    }

    // Constructor for city with club count
    public CityDTO(Long id, String name, int population, int clubCount) {
        this.id = id;
        this.name = name;
        this.population = population;
        this.clubCount = clubCount;
        this.formattedPopulation = formatPopulation(population);
        this.pointRadius = calculatePointRadius(population);
    }

    private String formatPopulation(int population) {
        if (population >= 1000000) {
            return String.format("%.1fM", population / 1000000.0);
        } else if (population >= 1000) {
            return String.format("%.0fK", population / 1000.0);
        }
        return String.valueOf(population);
    }

    private int calculatePointRadius(int population) {
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
