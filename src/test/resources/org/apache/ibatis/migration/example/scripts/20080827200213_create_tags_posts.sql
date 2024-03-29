--
--    Copyright 2010-2022 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       https://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

-- // First migration.
-- Migration SQL that makes the change goes here.

CREATE TABLE post (
id          INT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
blog_id     INT,
author_id   INT NOT NULL,
created_on  TIMESTAMP NOT NULL,
section     VARCHAR(25) NOT NULL,
subject     VARCHAR(255) NOT NULL,
body        CLOB NOT NULL,
PRIMARY KEY (id),
FOREIGN KEY (blog_id) REFERENCES blog(id)
);

CREATE TABLE tag (
id          INT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
name        VARCHAR(255) NOT NULL,
PRIMARY KEY (id)
);

CREATE TABLE post_tag (
post_id     INT NOT NULL,
tag_id      INT NOT NULL,
PRIMARY KEY (post_id, tag_id)
);

-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE post_tag;
DROP TABLE tag;
DROP TABLE post;


