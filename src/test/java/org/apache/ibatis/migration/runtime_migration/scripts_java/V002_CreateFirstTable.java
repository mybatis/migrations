/**
 *    Copyright 2010-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.migration.runtime_migration.scripts_java;

import org.apache.ibatis.migration.MigrationScript;

public class V002_CreateFirstTable implements MigrationScript {

  @Override
  public Long getId() {
    return Long.valueOf(this.getClass().getSimpleName().substring(1, 4));
  }

  @Override
  public String getDescription() {
    return "Create first table";
  }

  @Override
  public String getUpScript() {
    return "CREATE TABLE first_table (ID INTEGER NOT NULL, NAME VARCHAR(16));";
  }

  @Override
  public String getDownScript() {
    return "DROP TABLE first_table;";
  }

}
