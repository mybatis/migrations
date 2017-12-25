/**
 *    Copyright 2010-2017 the original author or authors.
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
package org.apache.ibatis.migration;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.SqlRunner;
import org.apache.ibatis.migration.utils.TestUtil;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class MigratorTest {

  @Rule
  public final ExpectedSystemExit exit = ExpectedSystemExit.none();

  @Rule
  public final SystemOutRule out = new SystemOutRule().enableLog();

  private static File dir;

  private static Properties env;

  @BeforeClass
  public static void setup() throws Exception {
    dir = Resources.getResourceAsFile("org/apache/ibatis/migration/example");
    env = Resources.getResourceAsProperties("org/apache/ibatis/migration/example/environments/development.properties");
  }

  @Test
  public void shouldRunThroughFullMigrationUseCaseInOneTestToEnsureOrder() throws Throwable {
    // Due to the nature of schema migrations, these tests must be run in order,
    // which is why they're executed from this single test. Perhaps there's a better way.

    exit.expectSystemExit();
    exit.checkAssertionAfterwards(new Assertion() {
      public void checkAssertion() {
        assertEquals("", out.getLog());
      }
    });

    testBootstrapCommand();
    testStatusContainsNoPendingEntriesUsingStatusShorthand();
    testUpCommandWithSpecifiedSteps();

    assertAuthorEmailContainsPlaceholder();

    testStatusContainsNoPendingMigrations();
    testDownCommandGiven2Steps();
    testStatusContainsPendingMigrations();

    testDoPendingScriptCommand();

    testVersionCommand();
    testStatusContainsNoPendingMigrations();
    testDownCommand();
    testStatusContainsPendingMigrations();
    testPendingCommand();
    testStatusContainsNoPendingMigrations();
    testHelpCommand();
    testDoScriptCommand();
    testUndoScriptCommand();

    out.clearLog();
    System.exit(0);
  }

  private void testBootstrapCommand() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "bootstrap", "--env=development"));
    String output = out.getLog();
    assertFalse(output.toString().contains("FAILURE"));
    assertTrue(output.toString().contains("-- // Bootstrap.sql"));
  }

  private void testStatusContainsNoPendingEntriesUsingStatusShorthand() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "sta"));
    String output = out.getLog();
    assertFalse(output.toString().contains("FAILURE"));
    assertTrue(output.toString().contains("...pending..."));
  }

  private void testUpCommandWithSpecifiedSteps() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "up", "3000"));
    String output = out.getLog();
    assertFalse(output.toString().contains("FAILURE"));
  }

  private void assertAuthorEmailContainsPlaceholder() throws Exception {
    final Connection conn = TestUtil.getConnection(env);
    try {
      final SqlRunner executor = new SqlRunner(conn);
      final Map<String, Object> author = executor.selectOne("select * from author where id = ?", 1);
      assertNotNull(author);
      assertNotNull(author.get("EMAIL"));
      assertEquals("jim@${url}", author.get("EMAIL"));
    } finally {
      conn.close();
    }
  }

  private void testDownCommandGiven2Steps() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "down", "2"));
    String output = out.getLog();
    assertFalse(output.toString().contains("FAILURE"));
  }

  private void testDoPendingScriptCommand() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "pending"));
    String output = out.getLog();
    assertTrue(output.toString().contains("INSERT"));
    assertTrue(output.toString().contains("CHANGELOG"));
    assertFalse(output.toString().contains("-- @UNDO"));

    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "pending_undo"));
    output = out.getLog();
    assertTrue(output.toString().contains("DELETE"));
    assertTrue(output.toString().contains("CHANGELOG"));
    assertTrue(output.toString().contains("-- @UNDO"));
  }

  private void testVersionCommand() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "version", "20080827200216"));
    String output = out.getLog();
    assertFalse(output.toString().contains("FAILURE"));
  }

  private void testDownCommand() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "down"));
    String output = out.getLog();
    assertFalse(output.toString().contains("FAILURE"));
  }

  private void testStatusContainsPendingMigrations() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "status"));
    String output = out.getLog();
    assertFalse(output.toString().contains("FAILURE"));
    assertTrue(output.toString().contains("...pending..."));
  }

  private void testPendingCommand() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "pending"));
    String output = out.getLog();
    assertFalse(output.toString().contains("FAILURE"));
  }

  private void testStatusContainsNoPendingMigrations() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "status"));
    String output = out.getLog();
    assertFalse(output.toString().contains("FAILURE"));
    assertFalse(output.toString().contains("...pending..."));
  }

  private void testHelpCommand() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "--help"));
    String output = out.getLog();
    assertFalse(output.toString().contains("FAILURE"));
    assertTrue(output.toString().contains("--help"));
  }

  private void testDoScriptCommand() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "20080827200212", "20080827200214"));
    String output = out.getLog();
    assertFalse(output.toString().contains("FAILURE"));
    assertFalse(output.toString().contains("20080827200210"));
    assertFalse(output.toString().contains("20080827200211"));
    assertFalse(output.toString().contains("20080827200212"));
    assertTrue(output.toString().contains("20080827200213"));
    assertTrue(output.toString().contains("20080827200214"));
    assertFalse(output.toString().contains("20080827200215"));
    assertFalse(output.toString().contains("-- @UNDO"));

    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "0", "20080827200211"));
    output = out.getLog();
    assertFalse(output.toString().contains("FAILURE"));
    assertTrue(output.toString().contains("20080827200210"));
    assertTrue(output.toString().contains("20080827200211"));
    assertFalse(output.toString().contains("20080827200212"));
    assertFalse(output.toString().contains("20080827200213"));
    assertFalse(output.toString().contains("20080827200214"));
    assertFalse(output.toString().contains("20080827200215"));
    assertFalse(output.toString().contains("-- @UNDO"));
  }

  private void testUndoScriptCommand() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "20080827200215", "20080827200213"));
    String output = out.getLog();
    assertFalse(output.toString().contains("FAILURE"));
    assertFalse(output.toString().contains("20080827200210"));
    assertFalse(output.toString().contains("20080827200211"));
    assertFalse(output.toString().contains("20080827200212"));
    assertFalse(output.toString().contains("20080827200213"));
    assertTrue(output.toString().contains("20080827200214"));
    assertTrue(output.toString().contains("20080827200215"));
    assertTrue(output.toString().contains("-- @UNDO"));
    out.clearLog();

    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "20080827200211", "0"));
    output = out.getLog();
    assertFalse(output.toString().contains("FAILURE"));
    assertTrue(output.toString().contains("20080827200210"));
    assertTrue(output.toString().contains("20080827200211"));
    assertFalse(output.toString().contains("20080827200212"));
    assertFalse(output.toString().contains("20080827200213"));
    assertFalse(output.toString().contains("20080827200214"));
    assertFalse(output.toString().contains("20080827200215"));
    assertTrue(output.toString().contains("-- @UNDO"));
  }

  @Test
  public void shouldScriptCommandFailIfSameVersion() throws Exception {
    exit.expectSystemExitWithStatus(1);
    exit.checkAssertionAfterwards(new Assertion() {
      public void checkAssertion() {
        String output = out.getLog();
        assertTrue(output.toString().contains("FAILURE"));
      }
    });
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "20080827200211", "20080827200211"));
  }

  @Test
  public void shouldInitTempDirectory() throws Exception {
    File basePath = getTempDir();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "init"));
    assertNotNull(basePath.list());
    assertEquals(4, basePath.list().length);
    File scriptPath = new File(basePath.getCanonicalPath() + File.separator + "scripts");
    assertEquals(3, scriptPath.list().length);
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "new", "test new migration"));
    assertEquals(4, scriptPath.list().length);
  }

  @Test
  public void shouldRespectIdPattern() throws Exception {
    String idPattern = "000";
    File basePath = getTempDir();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "--idpattern=" + idPattern, "init"));
    File changelog = new File(
        basePath.getCanonicalPath() + File.separator + "scripts" + File.separator + "001_create_changelog.sql");
    assertTrue(changelog.exists());
    Migrator.main(
        TestUtil.args("--path=" + basePath.getAbsolutePath(), "--idpattern=" + idPattern, "new", "new migration"));
    File newMigration = new File(
        basePath.getCanonicalPath() + File.separator + "scripts" + File.separator + "003_new_migration.sql");
    assertTrue(newMigration.exists());
  }

  @Test
  public void useCustomTemplate() throws Exception {
    String desc = "test new migration";
    File basePath = getTempDir();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "init"));
    assertNotNull(basePath.list());
    assertEquals(4, basePath.list().length);
    File scriptPath = new File(basePath.getCanonicalPath() + File.separator + "scripts");
    assertEquals(3, scriptPath.list().length);

    File templatePath = File.createTempFile("customTemplate", "sql");
    PrintWriter writer = new PrintWriter(templatePath);
    writer.println("// ${description}");
    writer.close();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "new", desc,
        "--template=" + templatePath.getAbsolutePath()));
    String[] scripts = scriptPath.list();
    Arrays.sort(scripts);
    assertEquals(4, scripts.length);
    Scanner scanner = new Scanner(new File(scriptPath, scripts[scripts.length - 2]));
    if (scanner.hasNextLine()) {
      assertEquals("// " + desc, scanner.nextLine());
    }

    templatePath.delete();
  }

  @Test
  public void useCustomTemplateWithNoValue() throws Exception {
    File basePath = getTempDir();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "init"));
    assertNotNull(basePath.list());
    assertEquals(4, basePath.list().length);
    File scriptPath = new File(basePath.getCanonicalPath() + File.separator + "scripts");
    assertEquals(3, scriptPath.list().length);

    File templatePath = File.createTempFile("customTemplate", "sql");
    templatePath.createNewFile();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "new", "test new migration", "--template="));
    assertEquals(4, scriptPath.list().length);
    templatePath.delete();
  }

  @Test
  public void useCustomTemplateWithBadPath() throws Exception {
    System.setProperty("migrationsHome", "/tmp");
    File basePath = getTempDir();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "init"));
    assertNotNull(basePath.list());
    assertEquals(4, basePath.list().length);
    File scriptPath = new File(basePath.getCanonicalPath() + File.separator + "scripts");
    assertEquals(3, scriptPath.list().length);

    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "new", "test new migration"));
    String output = out.getLog();
    assertEquals(4, scriptPath.list().length);
    assertTrue(output.toString()
        .contains("Your migrations configuration did not find your custom template.  Using the default template."));
  }

  @Test
  public void shouldSuppressOutputIfQuietOptionEnabled() throws Throwable {
    System.setProperty("migrationsHome", "/tmp");
    File basePath = getTempDir();
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "--quiet", "init"));
    String output = out.getLog();
    assertFalse(output.toString().contains("Initializing:"));
    assertNotNull(basePath.list());
  }

  @Test
  public void shouldColorizeSuccessOutputIfColorOptionEnabled() throws Throwable {
    System.setProperty("migrationsHome", "/tmp");
    File basePath = getTempDir();
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "--color", "init"));
    String output = out.getLog();
    assertTrue(output.toString().contains(ConsoleColors.GREEN + "SUCCESS"));
    assertNotNull(basePath.list());
  }

  @Test
  public void shouldColorizeFailureOutputIfColorOptionEnabled() throws Throwable {
    exit.expectSystemExitWithStatus(1);
    exit.checkAssertionAfterwards(new Assertion() {
      public void checkAssertion() {
        String output = out.getLog();
        assertTrue(output.toString().contains(ConsoleColors.RED + "FAILURE"));
      }
    });
    System.setProperty("migrationsHome", "/tmp");
    File basePath = getTempDir();
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "--color", "new"));
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
}
