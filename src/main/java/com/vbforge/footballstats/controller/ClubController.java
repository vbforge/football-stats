package com.vbforge.footballstats.controller;

import com.vbforge.footballstats.dto.club.ClubDetailDTO;
import com.vbforge.footballstats.entity.City;
import com.vbforge.footballstats.entity.Club;

import com.vbforge.footballstats.entity.Season;
import com.vbforge.footballstats.service.CityService;
import com.vbforge.footballstats.service.ClubService;
import com.vbforge.footballstats.service.SeasonService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clubs")
public class ClubController {

    private final ClubService clubService;
    private final CityService cityService;
    private final SeasonService seasonService;

    public ClubController(ClubService clubService, CityService cityService, SeasonService seasonService) {
        this.clubService = clubService;
        this.cityService = cityService;
        this.seasonService = seasonService;
    }

    // Club details
    @GetMapping("/{id}")
    public String viewClub(@PathVariable Long id, Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());
        try {
            ClubDetailDTO club = clubService.getClubDetail(id);
            model.addAttribute("club", club);
            City city = cityService.findCityByClubId(id).orElseThrow();
            model.addAttribute("city", city.getName());

            return "clubs/club_detail";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "Club not found");
            return "redirect:/league";
        }
    }

    // Edit club form
    @GetMapping("/{id}/edit")
    public String editClubForm(@PathVariable Long id, Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());
        try {
            Club club = clubService.getClubById(id);
            model.addAttribute("club", club);
            return "clubs/club_edit";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "Club not found");
            return "redirect:/league";
        }
    }

    /*// Update club
    @PostMapping("/{id}/update")
    public String updateClub(@PathVariable Long id,
                             @ModelAttribute Club club,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please fix the errors in the form");
            return "redirect:/clubs/" + id + "/edit";
        }

        try {
            club.setId(id);
            clubService.updateClub(club);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Club " + club.getName() + " updated successfully");
            return "redirect:/clubs/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating club: " + e.getMessage());
            return "redirect:/clubs/" + id + "/edit";
        }
    }*/

    @PostMapping("/{id}/update")
    public String updateClub(@PathVariable Long id,
                             @ModelAttribute Club club,
                             @RequestParam(value = "cityName", required = false) String cityName,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please fix the errors in the form");
            return "redirect:/clubs/" + id + "/edit";
        }

        try {
            club.setId(id);

            // Handle city if provided
            if (cityName != null && !cityName.trim().isEmpty()) {
                City city = cityService.getCityByName(cityName.trim())
                        .orElseGet(() -> {
                            City newCity = new City();
                            newCity.setName(cityName.trim());
                            return cityService.saveCity(newCity); // You'll need this method in CityService
                        });
                club.setCity(city);
            }

            clubService.updateClub(club);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Club " + club.getName() + " updated successfully");
            return "redirect:/clubs/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating club: " + e.getMessage());
            return "redirect:/clubs/" + id + "/edit";
        }
    }

}