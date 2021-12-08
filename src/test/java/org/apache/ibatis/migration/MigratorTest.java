/*
 *    Copyright 2010-2021 the original author or authors.
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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeSet;

import org.apache.ibatis.io.Resources;
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

    testRedoCommand();

    testDoPendingScriptCommand();

    testVersionCommand();
    testStatusContainsNoPendingMigrations();
    testSkippedScript();
    testMissingScript();
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
    assertFalse(output.contains("FAILURE"));
    assertTrue(output.contains("-- // Bootstrap.sql"));
  }

  private void testStatusContainsNoPendingEntriesUsingStatusShorthand() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "sta"));
    String output = out.getLog();
    assertFalse(output.contains("FAILURE"));
    assertTrue(output.contains("...pending..."));
  }

  private void testUpCommandWithSpecifiedSteps() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "up", "3000"));
    String output = out.getLog();
    assertFalse(output.contains("FAILURE"));
  }

  private void assertAuthorEmailContainsPlaceholder() throws Exception {
    try (final Connection conn = TestUtil.getConnection(env);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select EMAIL from author where id = 1")) {
      assertTrue(rs.next());
      assertEquals("jim@${url}", rs.getString("EMAIL"));
    }
  }

  private void testDownCommandGiven2Steps() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "down", "2"));
    String output = out.getLog();
    assertFalse(output.contains("FAILURE"));
  }

  private void testDoPendingScriptCommand() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "pending"));
    String output = out.getLog();
    assertTrue(output.contains("INSERT"));
    assertTrue(output.contains("CHANGELOG"));
    assertFalse(output.contains("-- @UNDO"));

    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "pending_undo"));
    output = out.getLog();
    assertTrue(output.contains("DELETE"));
    assertTrue(output.contains("CHANGELOG"));
    assertTrue(output.contains("-- @UNDO"));
  }

  private void testVersionCommand() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "version", "20080827200217"));
    String output = out.getLog();
    assertFalse(output.contains("FAILURE"));
  }

  private void testSkippedScript() throws Exception {
    out.clearLog();
    File skipped = new File(dir + File.separator + "scripts", "20080827200215_skipped_migration.sql");
    assertTrue(skipped.createNewFile());
    try {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "up"));
      String output = out.getLog();
      assertFalse(output.contains("FAILURE"));
      assertEquals(1, TestUtil.countStr(output, "WARNING"));
      assertTrue(output.contains(
          "WARNING: Migration script '20080827200215_skipped_migration.sql' was not applied to the database."));
    } finally {
      skipped.delete();
    }
  }

  private void testMissingScript() throws Exception {
    out.clearLog();
    File original = new File(dir + File.separator + "scripts", "20080827200216_create_procs.sql");
    File renamed = new File(dir + File.separator + "scripts", "20080827200216_create_procs._sql");
    assertTrue(original.renameTo(renamed));
    try {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "up"));
      String output = out.getLog();
      assertFalse(output.contains("FAILURE"));
      assertTrue(
          output.contains("WARNING: Missing migration script. id='20080827200216', description='create procs'."));
    } finally {
      assertTrue(renamed.renameTo(original));
    }
  }

  private void testDownCommand() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "down"));
    String output = out.getLog();
    assertFalse(output.contains("FAILURE"));
  }

  private void testRedoCommand() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "status"));
    String output = out.getLog();
    assertFalse(output.contains("20080827200214    ...pending..."));
    assertTrue(output.contains("20080827200216    ...pending..."));

    out.clearLog();
    {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "redo"));
      output = out.getLog();
      assertFalse(output.contains("FAILURE"));
      assertEquals("Should down be just one step", -1, output.indexOf("DROP TABLE post_tag"));
      int dropIdx = output.indexOf("DROP TABLE comment");
      int createIdx = output.indexOf("CREATE TABLE comment (");
      assertNotEquals(-1, dropIdx);
      assertNotEquals(-1, createIdx);
      assertTrue(dropIdx < createIdx);
    }

    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "status"));
    output = out.getLog();
    assertFalse(output.contains("20080827200214    ...pending..."));
    assertTrue(output.contains("20080827200216    ...pending..."));

    out.clearLog();
    {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "redo", "2"));
      output = out.getLog();
      assertFalse(output.contains("FAILURE"));
      assertEquals("Should down be two steps", -1, output.indexOf("DROP TABLE blog"));
      List<Integer> lineNums = new ArrayList<>();
      lineNums.add(output.indexOf("DROP TABLE comment"));
      lineNums.add(output.indexOf("DROP TABLE post"));
      lineNums.add(output.indexOf("CREATE TABLE post ("));
      lineNums.add(output.indexOf("CREATE TABLE comment ("));
      assertEquals(new TreeSet<>(lineNums).toString(), lineNums.toString());
    }

    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "status"));
    output = out.getLog();
    assertFalse(output.contains("20080827200214    ...pending..."));
    assertTrue(output.contains("20080827200216    ...pending..."));
  }

  private void testStatusContainsPendingMigrations() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "status"));
    String output = out.getLog();
    assertFalse(output.contains("FAILURE"));
    assertTrue(output.contains("...pending..."));
  }

  private void testPendingCommand() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "pending"));
    String output = out.getLog();
    assertFalse(output.contains("FAILURE"));
  }

  private void testStatusContainsNoPendingMigrations() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "status"));
    String output = out.getLog();
    assertFalse(output.contains("FAILURE"));
    assertFalse(output.contains("...pending..."));
  }

  private void testHelpCommand() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "--help"));
    String output = out.getLog();
    assertFalse(output.contains("FAILURE"));
    assertTrue(output.contains("--help"));
  }

  private void testDoScriptCommand() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "20080827200212", "20080827200214"));
    String output = out.getLog();
    assertFalse(output.contains("FAILURE"));
    assertFalse(output.contains("20080827200210"));
    assertFalse(output.contains("20080827200211"));
    assertFalse(output.contains("20080827200212"));
    assertTrue(output.contains("20080827200213"));
    assertTrue(output.contains("20080827200214"));
    assertFalse(output.contains("20080827200216"));
    assertFalse(output.contains("-- @UNDO"));

    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "0", "20080827200211"));
    output = out.getLog();
    assertFalse(output.contains("FAILURE"));
    assertTrue(output.contains("20080827200210"));
    assertTrue(output.contains("20080827200211"));
    assertFalse(output.contains("20080827200212"));
    assertFalse(output.contains("20080827200213"));
    assertFalse(output.contains("20080827200214"));
    assertFalse(output.contains("20080827200216"));
    assertFalse(output.contains("-- @UNDO"));
  }

  private void testUndoScriptCommand() throws Exception {
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "20080827200216", "20080827200213"));
    String output = out.getLog();
    assertFalse(output.contains("FAILURE"));
    assertFalse(output.contains("20080827200210"));
    assertFalse(output.contains("20080827200211"));
    assertFalse(output.contains("20080827200212"));
    assertFalse(output.contains("20080827200213"));
    assertTrue(output.contains("20080827200214"));
    assertTrue(output.contains("20080827200216"));
    assertTrue(output.contains("-- @UNDO"));
    out.clearLog();

    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "20080827200211", "0"));
    output = out.getLog();
    assertFalse(output.contains("FAILURE"));
    assertTrue(output.contains("20080827200210"));
    assertTrue(output.contains("20080827200211"));
    assertFalse(output.contains("20080827200212"));
    assertFalse(output.contains("20080827200213"));
    assertFalse(output.contains("20080827200214"));
    assertFalse(output.contains("20080827200216"));
    assertFalse(output.contains("DELETE FROM CHANGELOG WHERE ID = 20080827200210;"));
    assertTrue(output.contains("-- @UNDO"));
  }

  @Test
  public void shouldScriptCommandFailIfSameVersion() throws Exception {
    exit.expectSystemExitWithStatus(1);
    exit.checkAssertionAfterwards(new Assertion() {
      public void checkAssertion() {
        String output = out.getLog();
        assertTrue(output.contains("FAILURE"));
      }
    });
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "20080827200211", "20080827200211"));
  }

  @Test
  public void shouldInitTempDirectory() throws Exception {
    File basePath = TestUtil.getTempDir();
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
    File basePath = TestUtil.getTempDir();
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
    File basePath = TestUtil.getTempDir();
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
    try (Scanner scanner = new Scanner(new File(scriptPath, scripts[scripts.length - 2]))) {
      if (scanner.hasNextLine()) {
        assertEquals("// " + desc, scanner.nextLine());
      }
    }

    templatePath.delete();
  }

  @Test
  public void useCustomTemplateWithNoValue() throws Exception {
    File basePath = TestUtil.getTempDir();
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
    File basePath = TestUtil.getTempDir();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "init"));
    assertNotNull(basePath.list());
    assertEquals(4, basePath.list().length);
    File scriptPath = new File(basePath.getCanonicalPath() + File.separator + "scripts");
    assertEquals(3, scriptPath.list().length);

    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "new", "test new migration"));
    String output = out.getLog();
    assertEquals(4, scriptPath.list().length);
    assertTrue(output
        .contains("Your migrations configuration did not find your custom template.  Using the default template."));
  }

  @Test
  public void shouldSuppressOutputIfQuietOptionEnabled() throws Throwable {
    System.setProperty("migrationsHome", "/tmp");
    File basePath = TestUtil.getTempDir();
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "--quiet", "init"));
    String output = out.getLog();
    assertFalse(output.contains("Initializing:"));
    assertNotNull(basePath.list());
  }

  @Test
  public void shouldColorizeSuccessOutputIfColorOptionEnabled() throws Throwable {
    System.setProperty("migrationsHome", "/tmp");
    File basePath = TestUtil.getTempDir();
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "--color", "init"));
    String output = out.getLog();
    assertTrue(output.contains(ConsoleColors.GREEN + "SUCCESS"));
    assertNotNull(basePath.list());
  }

  @Test
  public void shouldColorizeFailureOutputIfColorOptionEnabled() throws Throwable {
    exit.expectSystemExitWithStatus(1);
    exit.checkAssertionAfterwards(new Assertion() {
      public void checkAssertion() {
        String output = out.getLog();
        assertTrue(output.contains(ConsoleColors.RED + "FAILURE"));
      }
    });
    System.setProperty("migrationsHome", "/tmp");
    File basePath = TestUtil.getTempDir();
    out.clearLog();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "--color", "new"));
  }
}
