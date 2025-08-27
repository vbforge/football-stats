package com.vbforge.footballstats.repository;

import com.vbforge.footballstats.entity.MatchDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface MatchDayRepository extends JpaRepository<MatchDay, Long> {

    Optional<MatchDay> findByNumber(Integer number);

}
