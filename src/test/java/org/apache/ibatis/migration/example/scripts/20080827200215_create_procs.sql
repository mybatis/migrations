--
--    Copyright 2010-2016 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       http://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

-- // First migration.
-- Migration SQL that makes the change goes here.

CREATE PROCEDURE selectTwoSetsOfAuthors(DP1 INTEGER, DP2 INTEGER)
PARAMETER STYLE JAVA
LANGUAGE JAVA
READS SQL DATA
DYNAMIC RESULT SETS 2
EXTERNAL NAME 'databases.blog.StoredProcedures.selectTwoSetsOfTwoAuthors';

CREATE PROCEDURE insertAuthor(DP1 INTEGER, DP2 VARCHAR(255), DP3 VARCHAR(255), DP4 VARCHAR(255))
PARAMETER STYLE JAVA
LANGUAGE JAVA
EXTERNAL NAME 'databases.blog.StoredProcedures.insertAuthor';

CREATE PROCEDURE selectAuthorViaOutParams(ID INTEGER, OUT USERNAME VARCHAR(255), OUT PASSWORD VARCHAR(255), OUT EMAIL VARCHAR(255), OUT BIO VARCHAR(255))
PARAMETER STYLE JAVA
LANGUAGE JAVA
EXTERNAL NAME 'databases.blog.StoredProcedures.selectAuthorViaOutParams';

-- //@UNDO
-- SQL to undo the change goes here.

DROP PROCEDURE selectTwoSetsOfAuthors;
DROP PROCEDURE insertAuthor;
DROP PROCEDURE selectAuthorViaOutParams;

