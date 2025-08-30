package com.vbforge.footballstats.controller;

import com.vbforge.footballstats.dto.ClubDetailDTO;
import com.vbforge.footballstats.entity.Club;
import com.vbforge.footballstats.service.FootballStatsService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clubs")
public class ClubController {

    private FootballStatsService footballStatsService;

    public ClubController(FootballStatsService footballStatsService) {
        this.footballStatsService = footballStatsService;
    }

    @GetMapping("/{id}")
    public String clubDetails(@PathVariable Long id, Model model) {
        try {
            ClubDetailDTO clubDetail = footballStatsService.getClubDetail(id);
            model.addAttribute("club", clubDetail);
            return "club-detail";
        } catch (EntityNotFoundException e) {
            return "redirect:/league?error=Club not found";
        }
    }

    @GetMapping("/{id}/edit")
    public String editClubForm(@PathVariable Long id, Model model) {
        try {
            Club club = footballStatsService.getClubById(id);
            model.addAttribute("club", club);
            return "club-edit";
        } catch (EntityNotFoundException e) {
            return "redirect:/league?error=Club not found";
        }
    }

    @PostMapping("/{id}/update")
    public String updateClub(@PathVariable Long id, @ModelAttribute Club club,
                             RedirectAttributes redirectAttributes) {
        try {
            club.setId(id);
            footballStatsService.updateClub(club);
            redirectAttributes.addFlashAttribute("successMessage", "Club updated successfully!");
            return "redirect:/clubs/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating club: " + e.getMessage());
            return "redirect:/clubs/" + id + "/edit";
        }
    }

}
