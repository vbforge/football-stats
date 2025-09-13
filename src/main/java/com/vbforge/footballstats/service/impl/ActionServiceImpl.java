package com.vbforge.footballstats.service.impl;

import com.vbforge.footballstats.dto.ActionFormDTO;
import com.vbforge.footballstats.entity.*;
import com.vbforge.footballstats.repository.*;
import com.vbforge.footballstats.service.ActionService;
import org.springframework.stereotype.Service;

@Service
public class ActionServiceImpl implements ActionService {

    private final SeasonRepository seasonRepository;
    private final MatchDayRepository matchDayRepository;
    private final ClubRepository clubRepository;
    private final PlayerRepository playerRepository;
    private final ActionRepository actionRepository;

    public ActionServiceImpl(SeasonRepository seasonRepository,
                             MatchDayRepository matchDayRepository,
                             ClubRepository clubRepository,
                             PlayerRepository playerRepository,
                             ActionRepository actionRepository) {
        this.seasonRepository = seasonRepository;
        this.matchDayRepository = matchDayRepository;
        this.clubRepository = clubRepository;
        this.playerRepository = playerRepository;
        this.actionRepository = actionRepository;
    }

    @Override
    public void saveAction(ActionFormDTO actionForm) {
        // Get current season
        Season currentSeason = seasonRepository.findByIsCurrentTrue()
                .orElseThrow(() -> new IllegalStateException("No current season is set"));

        // Get or create match day for current season
        MatchDay matchDay = matchDayRepository.findByNumberAndSeason(actionForm.getMatchDayNumber(), currentSeason)
                .orElseGet(() -> {
                    MatchDay newMatchDay = new MatchDay();
                    newMatchDay.setNumber(actionForm.getMatchDayNumber());
                    newMatchDay.setSeason(currentSeason);
                    return matchDayRepository.save(newMatchDay);
                });

        Player player;
        if (actionForm.isNewPlayer()) {
            // Create new player - but first validate club exists
            Club club = clubRepository.findByName(actionForm.getClubName())
                    .orElseThrow(() -> new IllegalArgumentException("Club not found: " + actionForm.getClubName()));

            player = new Player();
            player.setName(actionForm.getPlayerName());
            player.setClub(club);
            player = playerRepository.save(player);
        } else {
            player = playerRepository.findById(actionForm.getPlayerId())
                    .orElseThrow(() -> new RuntimeException("Player not found"));
        }

        // Create action
        Action action = new Action();
        action.setPlayer(player);
        action.setMatchDay(matchDay);
        action.setGoals(actionForm.getGoals());
        action.setAssists(actionForm.getAssists());

        actionRepository.save(action);
    }
}
