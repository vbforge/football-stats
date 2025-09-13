-- Verification queries to check the city-club mapping
-- Run these after the migration to ensure everything is set up correctly

-- 1. Check all cities with their club counts
SELECT
    c.name as city_name,
    c.population,
    c.x_coordinate,
    c.y_coordinate,
    COUNT(cl.id) as club_count
FROM cities c
         LEFT JOIN clubs cl ON c.id = cl.city_id
GROUP BY c.id, c.name, c.population, c.x_coordinate, c.y_coordinate
ORDER BY club_count DESC, c.population DESC;

-- 2. List all clubs with their cities
SELECT
    cl.name as club_name,
    c.name as city_name,
    c.population as city_population
FROM clubs cl
         LEFT JOIN cities c ON cl.city_id = c.id
ORDER BY c.name, cl.name;

-- 3. Find clubs without cities (these need manual assignment)
SELECT
    name as club_name,
    city as old_city_column
FROM clubs
WHERE city_id IS NULL;

-- 4. Cities with most clubs
SELECT
    c.name as city_name,
    COUNT(cl.id) as club_count,
    GROUP_CONCAT(cl.name SEPARATOR ', ') as clubs
FROM cities c
         JOIN clubs cl ON c.id = cl.city_id
GROUP BY c.id, c.name
HAVING club_count > 1
ORDER BY club_count DESC;

-- 5. Verify the data types and constraints
DESCRIBE cities;
DESCRIBE clubs;

-- 6. Check for any data inconsistencies
SELECT
    'Cities without clubs' as check_type,
    COUNT(*) as count
FROM cities c
         LEFT JOIN clubs cl ON c.id = cl.city_id
WHERE cl.id IS NULL

UNION ALL

SELECT
    'Clubs without cities' as check_type,
    COUNT(*) as count
FROM clubs
WHERE city_id IS NULL;

-- 7. Sample data for testing the application
SELECT
    c.name as city_name,
    c.x_coordinate,
    c.y_coordinate,
    c.population,
    c.description,
    cl.name as club_name,
    cl.founded_year,
    cl.stadium
FROM cities c
         LEFT JOIN clubs cl ON c.id = cl.city_id
ORDER BY c.population DESC, c.name, cl.name;