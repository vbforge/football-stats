package com.vbforge.footballstats.repository;

import com.vbforge.footballstats.dto.PlayerStatisticsDTO;
import com.vbforge.footballstats.entity.Player;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findByClub_Id(Long clubId);

}
