-- // First migration.
-- Migration SQL that makes the change goes here.

CREATE TABLE first_table (
ID INTEGER NOT NULL,
NAME VARCHAR(16)
);


-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE first_table;
