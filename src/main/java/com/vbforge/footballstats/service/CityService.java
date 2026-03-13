package com.vbforge.footballstats.service;

import com.vbforge.footballstats.entity.City;
import com.vbforge.footballstats.entity.Club;

import java.util.List;
import java.util.Optional;

public interface CityService {

    List<City> getAllCities();

    Optional<City> getCityById(Long id);

    Optional<City> getCityByName(String name);

    City saveCity(City city);

    void updateCity(City city);

    void deleteCity(Long id);

    List<Club> getClubsByCity(Long cityId);

    List<City> getCitiesWithClubs();

    List<City> getCitiesByPopulationRange(int minPopulation, int maxPopulation);

    List<City> getCitiesByCoordinates(int minX, int maxX, int minY, int maxY);

    boolean isCityNameUnique(String name);

    int getClubCountByCity(Long cityId);

    List<City> getMostPopulatedCities(int limit);

    List<City> searchCitiesByName(String searchTerm);

    Optional<City> findCityByClubId(Long clubId);


}
