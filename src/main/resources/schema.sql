DROP TABLE IF EXISTS PLAYERS;

-- Create a table from the csv
CREATE TABLE PLAYERS AS SELECT * FROM CSVREAD('Player.csv');

-- Indexes for common lookups
CREATE INDEX idx_players_lastname  ON PLAYERS(NAMELAST);
CREATE INDEX idx_players_firstname ON PLAYERS(NAMEFIRST);
CREATE INDEX idx_players_debut     ON PLAYERS(DEBUT);
CREATE INDEX idx_players_birthcountry ON PLAYERS(BIRTHCOUNTRY);