package org.apache.ibatis.migration.runtime_migration.scripts_java;

import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.JavaMigrationLoader;
import org.junit.Test;

public class JavaMigrationLoaderTest {

  @Test
  public void testGetMigrations() throws Exception {
    JavaMigrationLoader loader = createMigrationLoader();
    List<Change> migrations = loader.getMigrations();
    assertEquals(3, migrations.size());
  }

  @Test
  public void testGetScriptReader() throws Exception {
    JavaMigrationLoader loader = createMigrationLoader();
    Change change = new Change();
    change.setFilename("org.apache.ibatis.migration.runtime_migration.scripts_java.V002_CreateFirstTable");
    Reader reader = loader.getScriptReader(change, false);
    Writer writer = new StringWriter();
    int c;
    while ((c = reader.read()) != -1) {
      writer.write(c);
    }
    assertTrue(writer.toString().indexOf("CREATE TABLE first_table (ID INTEGER NOT NULL, NAME VARCHAR(16));") > -1);
  }

  @Test
  public void testGetBootstrapReader() throws Exception {
    JavaMigrationLoader loader = createMigrationLoader();
    Reader reader = loader.getBootstrapReader();
    Writer writer = new StringWriter();
    int c;
    while ((c = reader.read()) != -1) {
      writer.write(c);
    }
    assertTrue(writer.toString().indexOf("CREATE TABLE bootstrap_table (ID INTEGER NOT NULL, NAME VARCHAR(16));") > -1);
  }

  protected JavaMigrationLoader createMigrationLoader() {
    JavaMigrationLoader loader = new JavaMigrationLoader("org.apache.ibatis.migration.runtime_migration.scripts_java");
    return loader;
  }
}
