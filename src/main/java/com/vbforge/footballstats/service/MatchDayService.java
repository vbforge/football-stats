package com.vbforge.footballstats.service;

import com.vbforge.footballstats.entity.MatchDay;

import java.util.Optional;

public interface MatchDayService {

    Optional<MatchDay> getMatchDayById(Long matchDayId);

}
