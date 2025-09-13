package com.vbforge.footballstats.service;

import com.vbforge.footballstats.dto.ClubDetailDTO;
import com.vbforge.footballstats.entity.City;
import com.vbforge.footballstats.entity.Club;

import java.util.List;
import java.util.Optional;

public interface ClubService {

    List<Club> getAllClubs();
    ClubDetailDTO getClubDetail(Long clubId);
    Club getClubById(Long clubId);
    void updateClub(Club club);
    Optional<City> getCityByClubId(Long clubId);

}
