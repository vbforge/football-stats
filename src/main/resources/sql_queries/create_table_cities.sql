-- Migration script to create cities table and update clubs table
-- V2_001__Create_Cities_And_Update_Clubs.sql

-- Step 1: Create cities table
CREATE TABLE cities (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL UNIQUE,
                        x_coordinate INT NOT NULL,
                        y_coordinate INT NOT NULL,
                        population INT NOT NULL,
                        description TEXT
);

-- Step 2: Insert city data
INSERT INTO cities (name, x_coordinate, y_coordinate, population, description) VALUES
                                                                                   ('London', 150, 260, 9000000, 'Capital of Great Britain'),
                                                                                   ('Manchester', 107, 180, 553000, 'Major city in North West England'),
                                                                                   ('Liverpool', 93, 185, 498000, 'Historic port city, Merseyside'),
                                                                                   ('Birmingham', 120, 230, 1140000, 'Second largest UK city'),
                                                                                   ('Leeds', 118, 171, 793000, 'Financial and cultural hub in West Yorkshire'),
                                                                                   ('Newcastle upon Tyne', 124, 130, 300000, 'Major city in North East England'),
                                                                                   ('Sunderland', 128, 140, 175000, 'Port city in Tyne and Wear'),
                                                                                   ('Nottingham', 130, 207, 323000, 'Known for Robin Hood legend'),
                                                                                   ('Wolverhampton', 110, 220, 263000, 'Industrial city in West Midlands'),
                                                                                   ('Brighton & Hove', 142, 280, 277000, 'Seaside resort city on the south coast'),
                                                                                   ('Burnley', 105, 169, 90000, 'Market town in Lancashire'),
                                                                                   ('Bournemouth', 100, 275, 196000, 'Coastal resort town in Dorset');

-- Step 3: Add city_id column to clubs table
ALTER TABLE clubs ADD COLUMN city_id BIGINT;

-- Step 4: Add foreign key constraint
ALTER TABLE clubs ADD CONSTRAINT FK_clubs_city_id
    FOREIGN KEY (city_id) REFERENCES cities(id);

-- Step 5: Update existing clubs with their city associations
-- London clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'London')
WHERE name IN ('Tottenham Hotspur', 'Arsenal', 'Fulham', 'Crystal Palace', 'West Ham United', 'Chelsea');

-- Manchester clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Manchester')
WHERE name IN ('Manchester City', 'Manchester United');

-- Liverpool clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Liverpool')
WHERE name IN ('Liverpool', 'Everton');

-- Birmingham clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Birmingham')
WHERE name = 'Aston Villa';

-- Leeds clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Leeds')
WHERE name = 'Leeds United';

-- Newcastle clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Newcastle upon Tyne')
WHERE name = 'Newcastle United';

-- Sunderland clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Sunderland')
WHERE name = 'Sunderland';

-- Nottingham clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Nottingham')
WHERE name = 'Nottingham Forest';

-- Wolverhampton clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Wolverhampton')
WHERE name = 'Wolverhampton Wanderers';

-- Brighton clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Brighton & Hove')
WHERE name = 'Brighton and Hove Albion';

-- Burnley clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Burnley')
WHERE name = 'Burnley';

-- Bournemouth clubs
UPDATE clubs SET city_id = (SELECT id FROM cities WHERE name = 'Bournemouth')
WHERE name = 'Bournemouth';

-- Step 6: Remove the old city column (after ensuring all data is migrated)
-- ALTER TABLE clubs DROP COLUMN city;