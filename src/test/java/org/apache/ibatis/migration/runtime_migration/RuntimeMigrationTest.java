/*
 *    Copyright 2010-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.migration.runtime_migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.FileMigrationLoader;
import org.apache.ibatis.migration.JdbcConnectionProvider;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.hook.MigrationHook;
import org.apache.ibatis.migration.operations.BootstrapOperation;
import org.apache.ibatis.migration.operations.DownOperation;
import org.apache.ibatis.migration.operations.PendingOperation;
import org.apache.ibatis.migration.operations.StatusOperation;
import org.apache.ibatis.migration.operations.UpOperation;
import org.apache.ibatis.migration.operations.VersionOperation;
import org.apache.ibatis.migration.options.DatabaseOperationOption;
import org.apache.ibatis.migration.utils.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RuntimeMigrationTest {
  private static final String driver = "org.hsqldb.jdbcDriver";
  private static final String url = "jdbc:hsqldb:mem:javaapitest";
  private static final String username = "sa";
  private static final String password = "";
  private JdbcConnectionProvider connectionProvider;
  private DatabaseOperationOption dbOption;
  private ByteArrayOutputStream out;
  private MigrationLoader migrationsLoader;
  private MigrationLoader migrationsLoaderFromOtherBranch;

  @BeforeEach
  void setup() {
    dbOption = new DatabaseOperationOption();
    out = new ByteArrayOutputStream();
    connectionProvider = new JdbcConnectionProvider(driver, url, username, password);
    migrationsLoader = createMigrationsLoader("org/apache/ibatis/migration/runtime_migration/scripts");
    migrationsLoaderFromOtherBranch = createMigrationsLoader(
        "org/apache/ibatis/migration/runtime_migration/scripts_from_other_branch");
  }

  @AfterEach
  void tearDown() throws Exception {
    runSql(connectionProvider, "shutdown");
  }

  @Test
  void testInitialStatus() {
    testClassLoaders().forEach(this::assertStatus);
  }

  private void assertStatus(ClassLoader classLoader) {
    ConnectionProvider provider = new JdbcConnectionProvider(classLoader, driver, url, username, password);
    StatusOperation status = new StatusOperation().operate(provider, migrationsLoader, dbOption, new PrintStream(out));
    assertEquals(0, status.getAppliedCount());
    assertEquals(3, status.getPendingCount());
    assertEquals(3, status.getCurrentStatus().size());
  }

  static Stream<ClassLoader> testClassLoaders() {
    return Stream.of(null, ClassLoader.getSystemClassLoader(), RuntimeMigrationTest.class.getClassLoader());
  }

  @Test
  void testBootstrapOperation() throws Exception {
    new BootstrapOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from bootstrap_table"));
  }

  @Test
  void shouldIgnoreBootstrapIfChangelogExists() {
    new UpOperation(1).operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));

    new BootstrapOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    assertTableDoesNotExist(connectionProvider, "bootstrap_table");
  }

  @Test
  void testUp() throws Exception {
    new UpOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    assertEquals("3", runQuery(connectionProvider, "select count(*) from changelog"));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from first_table"));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from second_table"));

    StatusOperation status = new StatusOperation().operate(connectionProvider, migrationsLoader, dbOption,
        new PrintStream(out));
    assertEquals(3, status.getAppliedCount());
    assertEquals(0, status.getPendingCount());
    assertEquals(3, status.getCurrentStatus().size());
  }

  @Test
  void testUpFromDifferentBranches() throws Exception {
    new UpOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    new UpOperation().operate(connectionProvider, migrationsLoaderFromOtherBranch, dbOption, new PrintStream(out));
    assertEquals("4", runQuery(connectionProvider, "select count(*) from changelog"));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from first_table"));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from second_table"));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from third_table"));

    StatusOperation status = new StatusOperation().operate(connectionProvider, migrationsLoader, dbOption,
        new PrintStream(out));
    assertEquals(3, status.getAppliedCount());
    assertEquals(0, status.getPendingCount());
    assertEquals(1, status.getMissingCount());
    assertEquals(4, status.getCurrentStatus().size());
  }

  @Test
  void testUpWithStep() throws Exception {
    new UpOperation(2).operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    assertEquals("2", runQuery(connectionProvider, "select count(*) from changelog"));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from first_table"));
    assertTableDoesNotExist(connectionProvider, "second_table");
  }

  @Test
  void testUpWithHook() throws Exception {
    final PrintStream printStream = new PrintStream(out);
    MigrationHook hook = new MigrationHook() {
      @Override
      public void beforeEach(Map<String, Object> bindingMap) {
        printStream.println("<BEFORE_EACH>");
      }

      @Override
      public void before(Map<String, Object> bindingMap) {
        printStream.println("<BEFORE>");
      }

      @Override
      public void afterEach(Map<String, Object> bindingMap) {
        printStream.println("<AFTER_EACH>");
      }

      @Override
      public void after(Map<String, Object> bindingMap) {
        printStream.println("<AFTER>");
      }
    };
    new UpOperation(3).operate(connectionProvider, migrationsLoader, dbOption, printStream, hook);
    String output = out.toString("utf-8");
    assertEquals(1, TestUtil.countStr(output, "<BEFORE>"));
    assertEquals(3, TestUtil.countStr(output, "<BEFORE_EACH>"));
    assertEquals(3, TestUtil.countStr(output, "<AFTER_EACH>"));
    assertEquals(1, TestUtil.countStr(output, "<AFTER>"));
    out.reset();
    new DownOperation(2).operate(connectionProvider, migrationsLoader, dbOption, printStream, hook);
    output = out.toString("utf-8");
    assertEquals(1, TestUtil.countStr(output, "<BEFORE>"));
    assertEquals(2, TestUtil.countStr(output, "<BEFORE_EACH>"));
    assertEquals(2, TestUtil.countStr(output, "<AFTER_EACH>"));
    assertEquals(1, TestUtil.countStr(output, "<AFTER>"));
  }

  @Test
  void testDown() throws Exception {
    new UpOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));

    new DownOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    assertEquals("2", runQuery(connectionProvider, "select count(*) from changelog"));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from first_table"));
    assertTableDoesNotExist(connectionProvider, "second_table");
  }

  @Test
  void testDownWithStep() throws Exception {
    new UpOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));

    new DownOperation(2).operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    assertEquals("1", runQuery(connectionProvider, "select count(*) from changelog"));
    assertTableDoesNotExist(connectionProvider, "first_table");
    assertTableDoesNotExist(connectionProvider, "second_table");
  }

  @Test
  void testPending() throws Exception {
    new UpOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));

    runSql(connectionProvider, "drop table first_table");
    runSql(connectionProvider, "delete from changelog where id = 20130707120738");

    new PendingOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    assertEquals("3", runQuery(connectionProvider, "select count(*) from changelog"));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from first_table"));
  }

  @Test
  void testVersionUp() throws Exception {
    // Need changelog.
    new UpOperation(1).operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));

    new VersionOperation(new BigDecimal("20130707120738")).operate(connectionProvider, migrationsLoader, dbOption,
        new PrintStream(out));
    assertEquals("2", runQuery(connectionProvider, "select count(*) from changelog"));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from first_table"));
    assertTableDoesNotExist(connectionProvider, "second_table");
  }

  @Test
  void testVersionDown() throws Exception {
    new UpOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));

    new VersionOperation(new BigDecimal("20130707120738")).operate(connectionProvider, migrationsLoader, dbOption,
        new PrintStream(out));
    assertEquals("2", runQuery(connectionProvider, "select count(*) from changelog"));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from first_table"));
    assertTableDoesNotExist(connectionProvider, "second_table");
  }

  protected void assertTableDoesNotExist(ConnectionProvider connectionProvider, String table) {
    try {
      runQuery(connectionProvider, "select count(*) from " + table);
      fail();
    } catch (SQLException e) {
      // expected
    }
  }

  protected FileMigrationLoader createMigrationsLoader(String resource) {
    URI uri = null;
    try {
      uri = getClass().getClassLoader().getResource(resource).toURI();
    } catch (URISyntaxException e) {
      // Should not occur
      fail();
    }
    File scriptsDir = Path.of(uri).toFile();
    Properties properties = new Properties();
    properties.setProperty("changelog", "CHANGELOG");
    return new FileMigrationLoader(scriptsDir, "utf-8", properties);
  }

  protected void runSql(ConnectionProvider provider, String sql) throws SQLException {
    try (Connection connection = provider.getConnection(); Statement statement = connection.createStatement()) {
      statement.execute(sql);
    }
  }

  protected String runQuery(ConnectionProvider provider, String query) throws SQLException {
    try (Connection connection = provider.getConnection(); Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query)) {
      String result = null;
      if (rs.next()) {
        result = rs.getString(1);
      }
      return result;
    }
  }

}
