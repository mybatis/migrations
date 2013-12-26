package org.apache.ibatis.migration.runtime_migration;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.FileMigrationLoader;
import org.apache.ibatis.migration.JdbcConnectionProvider;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.operations.BootstrapOperation;
import org.apache.ibatis.migration.operations.DownOperation;
import org.apache.ibatis.migration.operations.PendingOperation;
import org.apache.ibatis.migration.operations.StatusOperation;
import org.apache.ibatis.migration.operations.UpOperation;
import org.apache.ibatis.migration.operations.VersionOperation;
import org.apache.ibatis.migration.options.DatabaseOperationOption;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RuntimeMigrationTest {

  private JdbcConnectionProvider connectionProvider;

  private DatabaseOperationOption dbOption;

  private ByteArrayOutputStream out;

  private MigrationLoader migrationsLoader;

  @Before
  public void setup() throws Exception {
    connectionProvider = new JdbcConnectionProvider("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:javaapitest", "sa", "");
    dbOption = new DatabaseOperationOption();
    out = new ByteArrayOutputStream();
    migrationsLoader = createMigrationsLoader();
  }

  @After
  public void tearDown() throws Exception {
    runSql(connectionProvider, "shutdown");
  }

  @Test
  public void testInitialStatus() throws Exception {
    StatusOperation status = new StatusOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    assertEquals(0, status.getAppliedCount());
    assertEquals(3, status.getPendingCount());
    assertEquals(3, status.getCurrentStatus().size());
  }

  @Test
  public void testBootstrapOperation() throws Exception {
    new BootstrapOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from bootstrap_table"));
  }

  @Test
  public void shouldIgnoreBootstrapIfChangelogExists() throws Exception {
    new UpOperation(1).operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));

    new BootstrapOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    assertTableDoesNotExist(connectionProvider, "bootstrap_table");
  }

  @Test
  public void testUp() throws Exception {
    new UpOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    assertEquals("3", runQuery(connectionProvider, "select count(*) from changelog"));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from first_table"));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from second_table"));

    StatusOperation status = new StatusOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    assertEquals(3, status.getAppliedCount());
    assertEquals(0, status.getPendingCount());
    assertEquals(3, status.getCurrentStatus().size());
  }

  @Test
  public void testUpWithStep() throws Exception {
    new UpOperation(2).operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    assertEquals("2", runQuery(connectionProvider, "select count(*) from changelog"));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from first_table"));
    assertTableDoesNotExist(connectionProvider, "second_table");
  }

  @Test
  public void testDown() throws Exception {
    new UpOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));

    new DownOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    assertEquals("2", runQuery(connectionProvider, "select count(*) from changelog"));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from first_table"));
    assertTableDoesNotExist(connectionProvider, "second_table");
  }

  @Test
  public void testDownWithStep() throws Exception {
    new UpOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));

    new DownOperation(2).operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    assertEquals("1", runQuery(connectionProvider, "select count(*) from changelog"));
    assertTableDoesNotExist(connectionProvider, "first_table");
    assertTableDoesNotExist(connectionProvider, "second_table");
  }

  @Test
  public void testPending() throws Exception {
    new UpOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));

    runSql(connectionProvider, "drop table first_table");
    runSql(connectionProvider, "delete from changelog where id = 20130707120738");

    new PendingOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    assertEquals("3", runQuery(connectionProvider, "select count(*) from changelog"));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from first_table"));
  }

  @Test
  public void testVersionUp() throws Exception {
    // Need changelog.
    new UpOperation(1).operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));

    new VersionOperation(new BigDecimal("20130707120738")).operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    assertEquals("2", runQuery(connectionProvider, "select count(*) from changelog"));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from first_table"));
    assertTableDoesNotExist(connectionProvider, "second_table");
  }

  @Test
  public void testVersionDown() throws Exception {
    new UpOperation().operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));

    new VersionOperation(new BigDecimal("20130707120738")).operate(connectionProvider, migrationsLoader, dbOption, new PrintStream(out));
    assertEquals("2", runQuery(connectionProvider, "select count(*) from changelog"));
    assertEquals("0", runQuery(connectionProvider, "select count(*) from first_table"));
    assertTableDoesNotExist(connectionProvider, "second_table");
  }

  protected void assertTableDoesNotExist(ConnectionProvider connectionProvider, String table) throws Exception {
    try {
      runQuery(connectionProvider, "select count(*) from " + table);
      fail();
    } catch (SQLException e) {
      // expected
    }
  }

  protected FileMigrationLoader createMigrationsLoader() {
    URL url = getClass().getClassLoader().getResource("org/apache/ibatis/migration/runtime_migration/scripts");
    File scriptsDir = new File(url.getFile());
    Properties properties = new Properties();
    properties.setProperty("changelog", "CHANGELOG");
    FileMigrationLoader migrationsLoader = new FileMigrationLoader(scriptsDir, "utf-8", properties);
    return migrationsLoader;
  }

  protected void runSql(ConnectionProvider provider, String sql) throws SQLException {
    Connection connection = provider.getConnection();
    try {
      Statement statement = connection.createStatement();
      statement.execute(sql);
    } finally {
      connection.close();
    }
  }

  protected String runQuery(ConnectionProvider provider, String query) throws SQLException {
    Connection connection = provider.getConnection();
    try {
      Statement statement = connection.createStatement();
      ResultSet rs = statement.executeQuery(query);
      String result = null;
      if (rs.next()) {
        result = rs.getString(1);
      }
      return result;
    } finally {
      connection.close();
    }
  }

}
