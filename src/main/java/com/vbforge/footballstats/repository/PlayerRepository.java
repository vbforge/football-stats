package com.vbforge.footballstats.repository;

import com.vbforge.footballstats.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByNameAndClubId(String name, Long clubId);

//    @Query("SELECT p FROM Player p WHERE p.club.id = :clubId")
//    List<Player> findByClubId(@Param("clubId") Long clubId);

    List<Player> findByClub_Id(Long clubId);

}
