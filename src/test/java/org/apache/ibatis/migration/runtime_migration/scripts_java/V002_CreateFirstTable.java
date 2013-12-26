package org.apache.ibatis.migration.runtime_migration.scripts_java;

import java.math.BigDecimal;

import org.apache.ibatis.migration.MigrationScript;

public class V002_CreateFirstTable implements MigrationScript {

  @Override
  public BigDecimal getId() {
    return new BigDecimal(this.getClass().getSimpleName().substring(1, 4));
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
