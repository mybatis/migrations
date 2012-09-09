package org.apache.ibatis.migration;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.SqlRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Permission;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

public class MigratorTest {

  public static final String BLOG_PROPERTIES = "databases/blog/blog-derby.properties";

  private static PrintStream out;
  private static StringOutputStream buffer;

  @BeforeClass
  public static void setup() throws Exception {
    out = System.out;
    buffer = new StringOutputStream();
    System.setOut(new PrintStream(buffer));

    DataSource ds = createUnpooledDataSource(BLOG_PROPERTIES);
    Connection conn = ds.getConnection();
    SqlRunner executor = new SqlRunner(conn);
    safeRun(executor, "DROP TABLE bootstrap");
    safeRun(executor, "DROP TABLE comment");
    safeRun(executor, "DROP TABLE post_tag");
    safeRun(executor, "DROP TABLE tag");
    safeRun(executor, "DROP TABLE post");
    safeRun(executor, "DROP TABLE blog");
    safeRun(executor, "DROP TABLE author");
    safeRun(executor, "DROP PROCEDURE selectTwoSetsOfAuthors");
    safeRun(executor, "DROP PROCEDURE insertAuthor");
    safeRun(executor, "DROP PROCEDURE selectAuthorViaOutParams");
    safeRun(executor, "DROP TABLE changelog");
    conn.commit();
    conn.close();

    System.setSecurityManager(new SecurityManager() {

      @Override
      public void checkPermission(Permission perm) {
      }

      @Override
      public void checkPermission(Permission perm, Object context) {
      }

      @Override
      public void checkExit(int status) {
        throw new RuntimeException("System exited with error code: " + status);
      }
    });
  }

  @AfterClass
  public static void teardown() {
    System.setOut(out);
    System.setSecurityManager(null);
  }

  @Test
  public void shouldRunThroughFullMigrationUseCaseInOneTestToEnsureOrder() throws Throwable {
    try {
      // Due to the nature of schema migrations, these tests must be run in order,
      // which is why they're executed from this single test.  Perhaps there's a better way.

      File f = getExampleDir();

      testBootstrapCommand(f);
      testStatusContainsNoPendingEntriesUsingStatusShorthand(f);
      testUpCommandWithSpecifiedSteps(f);

      assertAuthorEmailContainsPlaceholder();

      testStatusContainsNoPendingMigrations(f);
      testDownCommandGiven2Steps(f);
      testStatusContainsPendingMigrations(f);
      testVersionCommand(f);
      testStatusContainsNoPendingMigrations(f);
      testDownCommand(f);
      testStatusContainsPendingMigrations(f);
      testPendingCommand(f);
      testStatusContainsNoPendingMigrations(f);
      testHelpCommand(f);
      testDoScriptCommand(f);
      testUndoScriptCommand(f);

    } catch (Throwable t) {
      System.err.println(buffer);
      throw t;
    }
  }

  private void testBootstrapCommand(File f) throws Exception {
    safeMigratorMain(args("--path=" + f.getAbsolutePath(), "bootstrap", "--env=development"));
    assertFalse(buffer.toString().contains("FAILURE"));
    assertTrue(buffer.toString().contains("--  Bootstrap.sql"));
    buffer.clear();
  }

  private void testStatusContainsNoPendingEntriesUsingStatusShorthand(File f) throws Exception {
    safeMigratorMain(args("--path=" + f.getAbsolutePath(), "sta"));
    assertFalse(buffer.toString().contains("FAILURE"));
    assertTrue(buffer.toString().contains("...pending..."));
    buffer.clear();
  }

  private void testUpCommandWithSpecifiedSteps(File f) throws Exception {
    safeMigratorMain(args("--path=" + f.getAbsolutePath(), "up", "3000"));
    assertFalse(buffer.toString().contains("FAILURE"));
    buffer.clear();
  }

  private void assertAuthorEmailContainsPlaceholder() throws IOException, SQLException {
    final DataSource ds = createUnpooledDataSource(BLOG_PROPERTIES);
    final Connection conn = ds.getConnection();
    final SqlRunner executor = new SqlRunner(conn);
    final Map<String, Object> author = executor.selectOne("select * from author where id = ?", 1);
    assertNotNull(author);
    assertNotNull(author.get("EMAIL"));
    assertEquals("jim@${url}", author.get("EMAIL"));
  }

  private void testDownCommandGiven2Steps(File f) throws Exception {
    safeMigratorMain(args("--path=" + f.getAbsolutePath(), "down", "2"));
    assertFalse(buffer.toString().contains("FAILURE"));
    buffer.clear();
  }

  private void testVersionCommand(File f) throws Exception {
    safeMigratorMain(args("--path=" + f.getAbsolutePath(), "version", "20080827200215"));
    assertFalse(buffer.toString().contains("FAILURE"));
    buffer.clear();
  }

  private void testDownCommand(File f) throws Exception {
    safeMigratorMain(args("--path=" + f.getAbsolutePath(), "down"));
    assertFalse(buffer.toString().contains("FAILURE"));
    buffer.clear();
  }

  private void testStatusContainsPendingMigrations(File f) throws Exception {
    safeMigratorMain(args("--path=" + f.getAbsolutePath(), "status"));
    assertFalse(buffer.toString().contains("FAILURE"));
    assertTrue(buffer.toString().contains("...pending..."));
    buffer.clear();
  }

  private void testPendingCommand(File f) throws Exception {
    safeMigratorMain(args("--path=" + f.getAbsolutePath(), "pending"));
    assertFalse(buffer.toString().contains("FAILURE"));
    buffer.clear();
  }

  private void testStatusContainsNoPendingMigrations(File f) throws Exception {
    safeMigratorMain(args("--path=" + f.getAbsolutePath(), "status"));
    assertFalse(buffer.toString().contains("FAILURE"));
    assertFalse(buffer.toString().contains("...pending..."));
    buffer.clear();
  }

  private void testHelpCommand(File f) throws Exception {
    safeMigratorMain(args("--path=" + f.getAbsolutePath(), "--help"));
    assertFalse(buffer.toString().contains("FAILURE"));
    assertTrue(buffer.toString().contains("--help"));
    buffer.clear();
  }

  private void testDoScriptCommand(File f) throws Exception {
    safeMigratorMain(args("--path=" + f.getAbsolutePath(), "script", "20080827200212", "20080827200214"));
    assertFalse(buffer.toString().contains("FAILURE"));
    assertFalse(buffer.toString().contains("20080827200210"));
    assertFalse(buffer.toString().contains("20080827200211"));
    assertTrue(buffer.toString().contains("20080827200212"));
    assertTrue(buffer.toString().contains("20080827200213"));
    assertTrue(buffer.toString().contains("20080827200214"));
    assertFalse(buffer.toString().contains("20080827200215"));
    assertFalse(buffer.toString().contains("-- @UNDO"));
    buffer.clear();
  }

  private void testUndoScriptCommand(File f) throws Exception {
    safeMigratorMain(args("--path=" + f.getAbsolutePath(), "script", "20080827200215", "20080827200213"));
    assertFalse(buffer.toString().contains("FAILURE"));
    assertFalse(buffer.toString().contains("20080827200210"));
    assertFalse(buffer.toString().contains("20080827200211"));
    assertFalse(buffer.toString().contains("20080827200212"));
    assertTrue(buffer.toString().contains("20080827200213"));
    assertTrue(buffer.toString().contains("20080827200214"));
    assertTrue(buffer.toString().contains("20080827200215"));
    assertTrue(buffer.toString().contains("-- @UNDO"));
    buffer.clear();
  }

  private void safeMigratorMain(String[] args) throws Exception {
    // Handles System.exit(1) calls so that the JVM doesn't terminate during unit tests.
    // See security manager in setup method.
    try {
      Migrator.main(args);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void shouldInitTempDirectory() throws Exception {
    File basePath = getTempDir();
    safeMigratorMain(args("--path=" + basePath.getAbsolutePath(), "init"));
    assertNotNull(basePath.list());
    assertEquals(4, basePath.list().length);
    File scriptPath = new File(basePath.getCanonicalPath() + File.separator + "scripts");
    assertEquals(3, scriptPath.list().length);
    safeMigratorMain(args("--path=" + basePath.getAbsolutePath(), "new", "test new migration"));
    assertEquals(4, scriptPath.list().length);

  }

  @Test
  public void useCustomTemplate() throws Exception {
    File basePath = getTempDir();
    safeMigratorMain(args("--path=" + basePath.getAbsolutePath(), "init"));
    assertNotNull(basePath.list());
    assertEquals(4, basePath.list().length);
    File scriptPath = new File(basePath.getCanonicalPath() + File.separator + "scripts");
    assertEquals(3, scriptPath.list().length);

    File templatePath = File.createTempFile("customTemplate", "sql");
    templatePath.createNewFile();
    safeMigratorMain(args("--path=" + basePath.getAbsolutePath(), "new", "test new migration", "--template=" + templatePath.getAbsolutePath()));
    assertEquals(4, scriptPath.list().length);

    templatePath.delete();
  }

  @Test
  public void useCustomTemplateWithNoValue() throws Exception {
    File basePath = getTempDir();
    safeMigratorMain(args("--path=" + basePath.getAbsolutePath(), "init"));
    assertNotNull(basePath.list());
    assertEquals(4, basePath.list().length);
    File scriptPath = new File(basePath.getCanonicalPath() + File.separator + "scripts");
    assertEquals(3, scriptPath.list().length);

    File templatePath = File.createTempFile("customTemplate", "sql");
    templatePath.createNewFile();
    safeMigratorMain(args("--path=" + basePath.getAbsolutePath(), "new", "test new migration", "--template="));
    assertEquals(4, scriptPath.list().length);

    templatePath.delete();
  }

  @Test
  public void useCustomTemplateWithBadPath() throws Exception {
    System.setProperty("migrationHome", "/tmp");
    File basePath = getTempDir();
    safeMigratorMain(args("--path=" + basePath.getAbsolutePath(), "init"));
    assertNotNull(basePath.list());
    assertEquals(4, basePath.list().length);
    File scriptPath = new File(basePath.getCanonicalPath() + File.separator + "scripts");
    assertEquals(3, scriptPath.list().length);

    safeMigratorMain(args("--path=" + basePath.getAbsolutePath(), "new", "test new migration"));
    assertEquals(4, scriptPath.list().length);

    assertTrue(buffer.toString().contains("Your migrations configuration did not find your custom template.  Using the default template."));
  }

  private String[] args(String... args) {
    return args;
  }

  private File getExampleDir() throws IOException, URISyntaxException {
    URL resourceURL = Resources.getResourceURL(getClass().getClassLoader(), "org/apache/ibatis/migration/example/");
    File f = new File(resourceURL.toURI());
    assertTrue(f.exists());
    assertTrue(f.isDirectory());
    return f;
  }

  private File getTempDir() throws IOException {
    File f = File.createTempFile("migration", "test");
    assertTrue(f.delete());
    assertTrue(f.mkdir());
    assertTrue(f.exists());
    assertTrue(f.isDirectory());
    f.deleteOnExit();
    return f;
  }

  private static class StringOutputStream extends OutputStream {

    private StringBuilder builder = new StringBuilder();

    public void write(int b) throws IOException {
      builder.append((char) b);
//      out.write(b);
    }

    @Override
    public String toString() {
      return builder.toString();
    }

    public void clear() {
      builder.setLength(0);
    }
  }

  private static void safeRun(SqlRunner executor, String sql) {
    try {
      executor.run(sql);
    } catch (Exception e) {
      //ignore
    }
  }

  public static UnpooledDataSource createUnpooledDataSource(String resource) throws IOException {
    Properties props = Resources.getResourceAsProperties(resource);
    UnpooledDataSource ds = new UnpooledDataSource();
    ds.setDriver(props.getProperty("driver"));
    ds.setUrl(props.getProperty("url"));
    ds.setUsername(props.getProperty("username"));
    ds.setPassword(props.getProperty("password"));
    return ds;
  }


}
