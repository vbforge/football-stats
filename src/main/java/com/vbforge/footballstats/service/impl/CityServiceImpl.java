package com.vbforge.footballstats.service.impl;

import com.vbforge.footballstats.entity.City;
import com.vbforge.footballstats.entity.Club;
import com.vbforge.footballstats.repository.CityRepository;
import com.vbforge.footballstats.service.CityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    public CityServiceImpl(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Override
    public List<City> getAllCities() {
        log.debug("Fetching all cities");
        return cityRepository.findAll();
    }

    @Override
    public Optional<City> getCityById(Long id) {
        log.debug("Fetching city with id: {}", id);
        return cityRepository.findById(id);
    }

    @Override
    public Optional<City> getCityByName(String name) {
        log.debug("Fetching city with name: {}", name);
        return cityRepository.findByNameIgnoreCase(name);
    }

    @Override
    public Optional<City> findCityByClubId(Long clubId) {
        log.debug("Fetching city with club id: {}", clubId);
        return cityRepository.findCityByClubId(clubId);
    }

    @Override
    @Transactional
    public City saveCity(City city) {
        log.info("Saving new city: {}", city.getName());
        return cityRepository.save(city);
    }

    @Override
    @Transactional
    public void updateCity(City city) {
        log.info("Updating city with id: {}", city.getId());
        cityRepository.save(city);
    }

    @Override
    @Transactional
    public void deleteCity(Long id) {
        log.info("Deleting city with id: {}", id);
        if (!cityRepository.existsById(id)) {
            throw new RuntimeException("City not found with id: " + id);
        }
        cityRepository.deleteById(id);
    }

    @Override
    public List<Club> getClubsByCity(Long cityId) {
        log.debug("Fetching clubs for city id: {}", cityId);
        return cityRepository.findClubsByCityId(cityId);
    }

    @Override
    public List<City> getCitiesWithClubs() {
        log.debug("Fetching cities that have clubs");
        return cityRepository.findCitiesWithClubs();
    }

    @Override
    public List<City> getCitiesByPopulationRange(int minPopulation, int maxPopulation) {
        log.debug("Fetching cities with population between {} and {}", minPopulation, maxPopulation);
        return cityRepository.findByPopulationBetween(minPopulation, maxPopulation);
    }

    @Override
    public List<City> getCitiesByCoordinates(int minX, int maxX, int minY, int maxY) {
        log.debug("Fetching cities within coordinates: x({}-{}), y({}-{})", minX, maxX, minY, maxY);
        return cityRepository.findByCoordinateRange(minX, maxX, minY, maxY);
    }

    @Override
    public boolean isCityNameUnique(String name) {
        log.debug("Checking if city name is unique: {}", name);
        return !cityRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public int getClubCountByCity(Long cityId) {
        log.debug("Counting clubs for city id: {}", cityId);
        return cityRepository.countClubsByCityId(cityId);
    }

    @Override
    public List<City> getMostPopulatedCities(int limit) {
        log.debug("Fetching {} most populated cities", limit);
        return cityRepository.findTopByOrderByPopulationDesc(limit);
    }

    @Override
    public List<City> searchCitiesByName(String searchTerm) {
        log.debug("Searching cities with term: {}", searchTerm);
        return cityRepository.findByNameContainingIgnoreCase(searchTerm);
    }
}
