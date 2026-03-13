-- Migration script to handle potential club name variations
-- V2_002__Update_Club_Names_For_Mapping.sql

-- This script handles potential variations in club names that might exist in your database
-- Adjust these names to match exactly what's in your clubs table

-- Update clubs with city associations (flexible matching)
-- London clubs - handle potential name variations
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'London')
WHERE LOWER(name) LIKE '%tottenham%' OR LOWER(name) LIKE '%spurs%';

UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'London')
WHERE LOWER(name) LIKE '%arsenal%';

UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'London')
WHERE LOWER(name) LIKE '%fulham%';

UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'London')
WHERE LOWER(name) LIKE '%crystal palace%' OR LOWER(name) LIKE '%palace%';

UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'London')
WHERE LOWER(name) LIKE '%west ham%';

UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'London')
WHERE LOWER(name) LIKE '%chelsea%';

-- Manchester clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Manchester')
WHERE LOWER(name) LIKE '%manchester city%' OR LOWER(name) LIKE '%man city%';

UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Manchester')
WHERE LOWER(name) LIKE '%manchester united%' OR LOWER(name) LIKE '%man united%' OR LOWER(name) LIKE '%man utd%';

-- Liverpool clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Liverpool')
WHERE LOWER(name) LIKE '%liverpool%' AND LOWER(name) NOT LIKE '%everton%';

UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Liverpool')
WHERE LOWER(name) LIKE '%everton%';

-- Birmingham clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Birmingham')
WHERE LOWER(name) LIKE '%aston villa%' OR LOWER(name) LIKE '%villa%';

-- Leeds clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Leeds')
WHERE LOWER(name) LIKE '%leeds%';

-- Newcastle clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Newcastle upon Tyne')
WHERE LOWER(name) LIKE '%newcastle%';

-- Sunderland clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Sunderland')
WHERE LOWER(name) LIKE '%sunderland%';

-- Nottingham clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Nottingham')
WHERE LOWER(name) LIKE '%nottingham%' OR LOWER(name) LIKE '%forest%';

-- Wolverhampton clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Wolverhampton')
WHERE LOWER(name) LIKE '%wolverhampton%' OR LOWER(name) LIKE '%wolves%';

-- Brighton clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Brighton & Hove')
WHERE LOWER(name) LIKE '%brighton%';

-- Burnley clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Burnley')
WHERE LOWER(name) LIKE '%burnley%';

-- Bournemouth clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Bournemouth')
WHERE LOWER(name) LIKE '%bournemouth%';

-- Verify the updates by showing clubs without cities
-- SELECT name, city_id FROM clubs WHERE city_id IS NULL;

-- Optional: Add some additional Premier League clubs that might be missing
-- Uncomment and modify as needed

-- INSERT IGNORE INTO clubs (name, city_id, coach, founded_year) VALUES
-- ('Brentford', (SELECT id FROM cities WHERE name = 'London'), NULL, 1889),
-- ('Sheffield United', (SELECT id FROM cities WHERE name = 'Sheffield'), NULL, 1889),
-- ('Southampton', (SELECT id FROM cities WHERE name = 'Southampton'), NULL, 1885);

-- You might need to add these cities if they don't exist:
-- INSERT IGNORE INTO cities (name, x_coordinate, y_coordinate, population, description) VALUES
-- ('Sheffield', 125, 190, 584853, 'Steel city in South Yorkshire'),
-- ('Southampton', 123, 270, 253651, 'Port city on the south coast of England');