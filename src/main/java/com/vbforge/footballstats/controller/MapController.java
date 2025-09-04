package com.vbforge.footballstats.controller;

import com.vbforge.footballstats.entity.City;
import com.vbforge.footballstats.service.FootballStatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cities")
public class MapController {

    private FootballStatsService footballStatsService;

    public MapController(FootballStatsService footballStatsService) {
        this.footballStatsService = footballStatsService;
    }

    // These coordinates are based for UK Cities (within 800×600) viewBox SVG
    private final List<City> cities = List.of(
            new City("London", 150, 260, 9_000_000, "Capital of the Great Britain"),
            new City("Manchester", 107, 180, 553_000, "Major city in North West England"),
            new City("Liverpool", 93, 185, 498_000, "Historic port city, Merseyside"),
            new City("Birmingham", 120, 230, 1_140_000, "Second largest UK city"),
            new City("Leeds", 118, 171, 793_000, "Financial and cultural hub in West Yorkshire"),
            new City("Newcastle upon Tyne", 124, 130, 300_000, "Major city in North East England"),
            new City("Sunderland", 128, 140, 175_000, "Port city in Tyne and Wear"),
            new City("Nottingham", 130, 207, 323_000, "Known for Robin Hood legend"),
            new City("Wolverhampton", 110, 220, 263_000, "Industrial city in West Midlands"),
            new City("Brighton & Hove", 142, 280, 277_000, "Seaside resort city on the south coast"),
            new City("Burnley", 105, 169, 90_000, "Market town in Lancashire"),
            new City("Bournemouth", 100, 275, 196_000, "Coastal resort town in Dorset")
    );


    @GetMapping("/map")
    public String getMap(Model model) {
        model.addAttribute("cities", cities);
        model.addAttribute("totalCities", cities.size());
        return "map/interactive_map";
    }

    @GetMapping("/{cityName}")
    public String cityPage(@PathVariable String cityName, Model model) {
        Optional<City> city = cities.stream()
                .filter(c -> c.getName().equals(cityName))
                .findFirst();
        if (city.isPresent()) {
            model.addAttribute("city", city.get());
            model.addAttribute("cityName", cityName);
        }
        return "map/city";
    }

}