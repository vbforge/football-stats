package com.vbforge.footballstats.service.impl;

import com.vbforge.footballstats.entity.MatchDay;
import com.vbforge.footballstats.repository.MatchDayRepository;
import com.vbforge.footballstats.service.MatchDayService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MatchDayServiceImpl implements MatchDayService {

    private final MatchDayRepository matchDayRepository;

    public MatchDayServiceImpl(MatchDayRepository matchDayRepository) {
        this.matchDayRepository = matchDayRepository;
    }

    @Override
    public Optional<MatchDay> getMatchDayById(Long matchDayId) {
        return matchDayRepository.getMatchDayById(matchDayId);
    }

}
