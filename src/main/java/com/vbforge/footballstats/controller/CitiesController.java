package com.vbforge.footballstats.controller;

import com.vbforge.footballstats.entity.City;
import com.vbforge.footballstats.entity.Season;
import com.vbforge.footballstats.service.CityService;
import com.vbforge.footballstats.service.SeasonService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cities")
public class CitiesController {

    private final CityService cityService;
    private final SeasonService seasonService;

    public CitiesController(CityService cityService,
                            SeasonService seasonService) {
        this.cityService = cityService;
        this.seasonService = seasonService;
    }

    @GetMapping()
    public String getCitiesMap(Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());
        List<City> cities = cityService.getAllCities();
        model.addAttribute("cities", cities);
        model.addAttribute("totalCities", cities.size());
        return "map/interactive_map";
    }

    @GetMapping("/{cityName}")
    public String getCityPage(@PathVariable String cityName, Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());
        Optional<City> cityOptional = cityService.getCityByName(cityName);

        if (cityOptional.isPresent()) {
            City city = cityOptional.get();
            model.addAttribute("city", city);
            model.addAttribute("cityName", cityName);
            model.addAttribute("clubCount", cityService.getClubCountByCity(city.getId()));
            model.addAttribute("clubsCity", city.getClubs());
        } else {
            // Handle city not found
            model.addAttribute("cityName", cityName);
            model.addAttribute("error", "City not found");
        }

        return "map/city_page";
    }

}