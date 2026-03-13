package com.vbforge.footballstats.repository;

import com.vbforge.footballstats.entity.City;
import com.vbforge.footballstats.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    // Basic finder methods
    Optional<City> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    List<City> findByNameContainingIgnoreCase(String searchTerm);

    // Population-based queries
    List<City> findByPopulationBetween(int minPopulation, int maxPopulation);

    @Query("SELECT c FROM City c ORDER BY c.population DESC LIMIT :limit")
    List<City> findTopByOrderByPopulationDesc(@Param("limit") int limit);

    // Coordinate-based queries
    @Query("SELECT c FROM City c WHERE c.x BETWEEN :minX AND :maxX AND c.y BETWEEN :minY AND :maxY")
    List<City> findByCoordinateRange(@Param("minX") int minX, @Param("maxX") int maxX,
                                     @Param("minY") int minY, @Param("maxY") int maxY);

    // Club-related queries
    @Query("SELECT cl FROM Club cl WHERE cl.city.id = :cityId")
    List<Club> findClubsByCityId(@Param("cityId") Long cityId);

    @Query("SELECT COUNT(cl) FROM Club cl WHERE cl.city.id = :cityId")
    int countClubsByCityId(@Param("cityId") Long cityId);

    @Query("SELECT DISTINCT c FROM City c WHERE EXISTS (SELECT cl FROM Club cl WHERE cl.city.id = c.id)")
    List<City> findCitiesWithClubs();

    // Advanced queries for map visualization or analytics
    @Query("SELECT c FROM City c WHERE c.population > :minPopulation ORDER BY c.population DESC")
    List<City> findCitiesAbovePopulation(@Param("minPopulation") int minPopulation);

    @Query("SELECT c FROM City c JOIN c.clubs cl GROUP BY c ORDER BY COUNT(cl) DESC")
    List<City> findCitiesOrderByClubCount();

    @Query("SELECT ci FROM City ci JOIN Club cl ON ci.id = cl.city.id WHERE cl.id = :clubId")
    Optional<City> findCityByClubId(@Param("clubId") Long clubId);

}
