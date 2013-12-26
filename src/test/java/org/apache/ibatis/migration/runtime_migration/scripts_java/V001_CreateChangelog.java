package org.apache.ibatis.migration.runtime_migration.scripts_java;

import java.math.BigDecimal;

import org.apache.ibatis.migration.MigrationScript;

public class V001_CreateChangelog implements MigrationScript {

  @Override
  public BigDecimal getId() {
    return new BigDecimal(this.getClass().getSimpleName().substring(1, 4));
  }

  @Override
  public String getDescription() {
    return "Create changelog";
  }

  @Override
  public String getUpScript() {
    return "CREATE TABLE changelog ("
      + "ID NUMERIC(20,0) NOT NULL,"
      + "APPLIED_AT VARCHAR(25) NOT NULL,"
      + "DESCRIPTION VARCHAR(255) NOT NULL); "

      + "ALTER TABLE changelog "
      + "ADD CONSTRAINT PK_changelog "
      + "PRIMARY KEY (id);";
  }

  @Override
  public String getDownScript() {
    return "DROP TABLE changelog;";
  }

}
