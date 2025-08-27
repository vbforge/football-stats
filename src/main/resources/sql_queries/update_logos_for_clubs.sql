SELECT * FROM football_stats.clubs;
-- Check record exists
SELECT * FROM clubs WHERE name = 'Chelsea';

-- If found, run update again
UPDATE clubs SET logo_path = '/images/logos/Chelsea.svg' WHERE name = 'Chelsea';
UPDATE clubs SET logo_path = '/images/logos/Arsenal.svg' WHERE name = 'Arsenal';
UPDATE clubs SET logo_path = '/images/logos/Aston_Villa.svg' WHERE name = 'Aston Villa';
UPDATE clubs SET logo_path = '/images/logos/Bournemouth.svg' WHERE name = 'Bournemouth';
UPDATE clubs SET logo_path = '/images/logos/Brentford.svg' WHERE name = 'Brentford';
UPDATE clubs SET logo_path = '/images/logos/Brighton.svg' WHERE name = 'Brighton and Hove Albion';
UPDATE clubs SET logo_path = '/images/logos/Burnley.svg' WHERE name = 'Burnley';
UPDATE clubs SET logo_path = '/images/logos/Crystal_Palace.svg' WHERE name = 'Crystal Palace';
UPDATE clubs SET logo_path = '/images/logos/Everton.svg' WHERE name = 'Everton';
UPDATE clubs SET logo_path = '/images/logos/Fulham.svg' WHERE name = 'Fulham';
UPDATE clubs SET logo_path = '/images/logos/Leeds_United.svg' WHERE name = 'Leeds United';
UPDATE clubs SET logo_path = '/images/logos/Manchester_City.svg' WHERE name = 'Manchester City';
UPDATE clubs SET logo_path = '/images/logos/Manchester_United.svg' WHERE name = 'Manchester United';
UPDATE clubs SET logo_path = '/images/logos/Newcastle_United.svg' WHERE name = 'Newcastle United';
UPDATE clubs SET logo_path = '/images/logos/Sunderland.svg' WHERE name = 'Sunderland';
UPDATE clubs SET logo_path = '/images/logos/West_Ham_United.svg' WHERE name = 'West Ham United';
UPDATE clubs SET logo_path = '/images/logos/Wolverhampton_Wanderers.svg' WHERE name = 'Wolverhampton Wanderers';
UPDATE clubs SET logo_path = '/images/logos/Liverpool.svg' WHERE name = 'Liverpool';
UPDATE clubs SET logo_path = '/images/logos/Tottenham_Hotspur.svg' WHERE name = 'Tottenham Hotspur';
UPDATE clubs SET logo_path = '/images/logos/Nottingham_Forest.svg' WHERE name = 'Nottingham Forest';

-- Check result
SELECT * FROM clubs WHERE name = 'Chelsea';