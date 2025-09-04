package com.vbforge.footballstats.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class City {

    private String name;
    private int x; // X coordinate
    private int y; // Y coordinate
    private int population;
    private String description;

    //according to population the values could be changed
    public String getFormattedPopulation() {
        if (population >= 1000000) {
            return String.format("%.1fM", population / 1000000.0);
        } else if (population >= 1000) {
            return String.format("%.0fK", population / 1000.0);
        }
        return String.valueOf(population);
    }

    //according to population the values could be changed
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