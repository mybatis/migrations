--
--    Copyright 2010-2021 the original author or authors.
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

-- // Create Changelog

-- Default DDL for changelog table that will keep
-- a record of the migrations that have been run.

-- You can modify this to suit your database before
-- running your first migration.

-- Be sure that ID and DESCRIPTION fields exist in
-- BigInteger and String compatible fields respectively.

CREATE TABLE ${changelog} (
ID NUMERIC(20,0) NOT NULL,
APPLIED_AT VARCHAR(25) NOT NULL,
DESCRIPTION VARCHAR(255) NOT NULL
);

ALTER TABLE ${changelog}
ADD CONSTRAINT PK_${changelog}
PRIMARY KEY (id);

SELECT 'username: ' || USER_NAME FROM INFORMATION_SCHEMA.SYSTEM_USERS;
SELECT 'var1: ' || '${var1}' FROM (VALUES(0));
SELECT 'var2: ' || '${var2}' FROM (VALUES(0));
SELECT 'var3: ' || '${var3}' FROM (VALUES(0));
SELECT 'var4: ' || '${var4}' FROM (VALUES(0));
SELECT 'var5: ' || '${var5}' FROM (VALUES(0));
SELECT 'Var5: ' || '${Var5}' FROM (VALUES(0));
SELECT 'envvar1: ' || '${envvar1}' FROM (VALUES(0));


-- //@UNDO

DROP TABLE ${changelog};
