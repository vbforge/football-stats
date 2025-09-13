package com.vbforge.footballstats.controller;

import com.vbforge.footballstats.dto.ActionFormDTO;
import com.vbforge.footballstats.entity.Season;
import com.vbforge.footballstats.service.ActionService;
import com.vbforge.footballstats.service.ClubService;
import com.vbforge.footballstats.service.PlayerService;
import com.vbforge.footballstats.service.SeasonService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/action")
public class ActionController {

    private final ClubService clubService;
    private final PlayerService playerService;
    private final ActionService actionService;
    private final SeasonService seasonService;

    public ActionController(ClubService clubService,
                            PlayerService playerService,
                            ActionService actionService,
                            SeasonService seasonService) {
        this.clubService = clubService;
        this.playerService = playerService;
        this.actionService = actionService;
        this.seasonService = seasonService;
    }

    @GetMapping()
    public String action(Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());
        model.addAttribute("actionForm", new ActionFormDTO());
        model.addAttribute("clubs", clubService.getAllClubs());
        model.addAttribute("players", playerService.getAllPlayers());
        return "player_action";
    }

    @PostMapping("/add-action")
    public String addAction(@ModelAttribute ActionFormDTO actionForm,
                            RedirectAttributes redirectAttributes) {
        try {
            actionService.saveAction(actionForm);
            redirectAttributes.addFlashAttribute("success",
                    "Action added successfully for " + actionForm.getPlayerName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error adding action: " + e.getMessage());
        }
        return "redirect:/action";
    }

}
