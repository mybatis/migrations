﻿-- // Second migration.
-- Unicode test data جراءات امنية, استنفار امني
-- Migration SQL that makes the change goes here.

CREATE TABLE second_table (
ID INTEGER NOT NULL,
NAME VARCHAR(16)
);


-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE second_table;
