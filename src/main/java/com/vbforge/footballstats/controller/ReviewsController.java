package com.vbforge.footballstats.controller;

import com.vbforge.footballstats.entity.Season;
import com.vbforge.footballstats.service.SeasonService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/review")
public class ReviewsController {

    private final SeasonService seasonService;

    public ReviewsController(SeasonService seasonService) {
        this.seasonService = seasonService;
    }

    // Grid page with all match days
    @GetMapping
    public String allMatchDays(Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());

        // Create MD list 1..38
        List<Integer> matchDays = IntStream.rangeClosed(1, 38)
                .boxed()
                .collect(Collectors.toList());
        model.addAttribute("matchDays", matchDays);

        return "reviews/match-day_reviews";
    }

    // Single match day review
    @GetMapping("/{reviewId}")
    public String viewMatchDaysReview(@PathVariable Integer reviewId, Model model) {
        Season season = seasonService.getCurrentSeason().orElseThrow();
        model.addAttribute("seasonName", season.getName());
        model.addAttribute("reviewId", reviewId);

        // Build video path
        String videoPath = "/videos/MD-" + reviewId + ".mp4";
        model.addAttribute("videoPath", videoPath);

        return "reviews/match-day_video";
    }

}
