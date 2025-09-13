package com.vbforge.footballstats.service.impl;

import com.vbforge.footballstats.dto.ClubDetailDTO;
import com.vbforge.footballstats.dto.ClubStandingsDTO;
import com.vbforge.footballstats.dto.PlayerStatisticsDTO;
import com.vbforge.footballstats.entity.City;
import com.vbforge.footballstats.entity.Club;
import com.vbforge.footballstats.repository.ClubRepository;
import com.vbforge.footballstats.service.ClubService;
import com.vbforge.footballstats.service.PlayerService;
import com.vbforge.footballstats.service.StandingsService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;
    private final StandingsService standingsService;
    private final PlayerService playerService;

    public ClubServiceImpl(ClubRepository clubRepository, StandingsService standingsService, PlayerService playerService) {
        this.clubRepository = clubRepository;
        this.standingsService = standingsService;
        this.playerService = playerService;
    }

    @Override
    public List<Club> getAllClubs() {
        return clubRepository.findAll();
    }

    @Override
    public ClubDetailDTO getClubDetail(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new EntityNotFoundException("Club not found with id: " + clubId));

        ClubDetailDTO dto = new ClubDetailDTO();

        // Basic club info
        dto.setId(club.getId());
        dto.setName(club.getName());
        dto.setCoach(club.getCoach());
        dto.setLogoPath(club.getLogoPath());
        dto.setCity(String.valueOf(club.getCity()));
        dto.setFoundedYear(club.getFoundedYear());
        dto.setStadium(club.getStadium());
        dto.setStadiumCapacity(club.getStadiumCapacity());
        dto.setDescription(club.getDescription());
        dto.setWebsite(club.getWebsite());
        dto.setNickname(club.getNickname());
        dto.setStadiumImagePath(club.getStadiumImagePath());
        dto.setPrimaryColor(club.getPrimaryColor());
        dto.setSecondaryColor(club.getSecondaryColor());

        // Get club statistics from Games-based standings calculation
        ClubStandingsDTO clubStanding = standingsService.getClubStandingsForCurrentSeason(clubId);

        if (clubStanding != null) {
            dto.setMatchesPlayed(clubStanding.getMatchesPlayed());
            dto.setWins(clubStanding.getWins());
            dto.setDraws(clubStanding.getDraws());
            dto.setDefeats(clubStanding.getDefeats());
            dto.setTotalPoints(clubStanding.getTotalPoints());
            dto.setGoalsFor(clubStanding.getGoalsFor());
            dto.setGoalsAgainst(clubStanding.getGoalsAgainst());
            dto.setGoalDifference(clubStanding.getGoalDifference());

            // Calculate current position in league
            dto.setCurrentPosition(standingsService.getClubPositionInCurrentSeason(clubId));
        }

        // Get player statistics using existing methods
        dto.setTopScorers(playerService.getClubTopScorers(clubId, 5));
        dto.setTopAssisters(playerService.getClubTopAssisters(clubId, 5));

        // Calculate total players for this club
        List<PlayerStatisticsDTO> allClubPlayers = playerService.getPlayerStatisticsByClub(clubId);
        dto.setTotalPlayers(allClubPlayers.size());

        return dto;
    }

    @Override
    public Club getClubById(Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new EntityNotFoundException("Club not found with id: " + clubId));
    }

    @Override
    public void updateClub(Club club) {
        if (clubRepository.existsById(club.getId())) {
            clubRepository.save(club);
        } else {
            throw new EntityNotFoundException("Club not found with id: " + club.getId());
        }
    }

    @Override
    public Optional<City> getCityByClubId(Long clubId) {
        return Optional.empty();
    }
}
