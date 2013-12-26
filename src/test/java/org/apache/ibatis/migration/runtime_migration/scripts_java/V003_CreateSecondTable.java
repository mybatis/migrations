package org.apache.ibatis.migration.runtime_migration.scripts_java;

import java.math.BigDecimal;

import org.apache.ibatis.migration.MigrationScript;

public class V003_CreateSecondTable implements MigrationScript {

  @Override
  public BigDecimal getId() {
    return new BigDecimal(this.getClass().getSimpleName().substring(1, 4));
  }

  @Override
  public String getDescription() {
    return "Create second table";
  }

  @Override
  public String getUpScript() {
    return "CREATE TABLE second_table (ID INTEGER NOT NULL,NAME VARCHAR(16));";
  }

  @Override
  public String getDownScript() {
    return "DROP TABLE second_table;";
  }

}
