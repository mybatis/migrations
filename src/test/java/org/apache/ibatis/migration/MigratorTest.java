/*
 *    Copyright 2010-2023 the original author or authors.
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
package org.apache.ibatis.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.stefanbirkner.systemlambda.SystemLambda;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeSet;

import org.apache.ibatis.migration.io.Resources;
import org.apache.ibatis.migration.utils.TestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
class MigratorTest {

  private static File dir;

  private static Properties env;

  @BeforeAll
  static void setup() throws IOException {
    dir = Resources.getResourceAsFile("org/apache/ibatis/migration/example");
    env = Resources.getResourceAsProperties("org/apache/ibatis/migration/example/environments/development.properties");
  }

  @Test
  @Order(1)
  void testBootstrapCommand() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "bootstrap", "--env=development"));
    });
    assertFalse(output.contains("FAILURE"));
    assertTrue(output.contains("-- // Bootstrap.sql"));
  }

  @Test
  @Order(2)
  void testStatusContainsNoPendingEntriesUsingStatusShorthand() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "sta"));
    });
    assertFalse(output.contains("FAILURE"));
    assertTrue(output.contains("...pending..."));
  }

  // TODO This causes hsqldb 2.7.1 or 2.7.2 to blow up
  @Test
  @Order(3)
  void testUpCommandWithSpecifiedSteps() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "up", "3000"));
    });
    assertFalse(output.contains("FAILURE"));
  }

  @Test
  @Order(4)
  void assertAuthorEmailContainsPlaceholder() throws Exception {
    try (Connection conn = TestUtil.getConnection(env); Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select EMAIL from author where id = 1")) {
      assertTrue(rs.next());
      assertEquals("jim@${url}", rs.getString("EMAIL"));
    }
  }

  @Test
  @Order(5)
  void testDownCommandGiven2Steps() throws Exception {
    testStatusContainsNoPendingMigrations();
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "down", "2"));
    });
    assertFalse(output.contains("FAILURE"));
    testStatusContainsPendingMigrations();
  }

  @Test
  @Order(6)
  void testRedoCommand() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "status"));
    });
    assertFalse(output.contains("20080827200214    ...pending..."));
    assertTrue(output.contains("20080827200216    ...pending..."));

    output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "redo"));
    });
    assertFalse(output.contains("FAILURE"));
    assertEquals(-1, output.indexOf("DROP TABLE post_tag"), "Should down be just one step");
    int dropIdx = output.indexOf("DROP TABLE comment");
    int createIdx = output.indexOf("CREATE TABLE comment (");
    assertNotEquals(-1, dropIdx);
    assertNotEquals(-1, createIdx);
    assertTrue(dropIdx < createIdx);

    output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "status"));
    });
    assertFalse(output.contains("20080827200214    ...pending..."));
    assertTrue(output.contains("20080827200216    ...pending..."));

    output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "redo", "2"));
    });
    assertFalse(output.contains("FAILURE"));
    assertEquals(-1, output.indexOf("DROP TABLE blog"), "Should down be two steps");
    List<Integer> lineNums = new ArrayList<>();
    lineNums.add(output.indexOf("DROP TABLE comment"));
    lineNums.add(output.indexOf("DROP TABLE post"));
    lineNums.add(output.indexOf("CREATE TABLE post ("));
    lineNums.add(output.indexOf("CREATE TABLE comment ("));
    assertEquals(new TreeSet<>(lineNums).toString(), lineNums.toString());

    output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "status"));
    });
    assertFalse(output.contains("20080827200214    ...pending..."));
    assertTrue(output.contains("20080827200216    ...pending..."));
  }

  @Test
  @Order(7)
  void testDoPendingScriptCommand() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "pending"));
    });
    assertTrue(output.contains("INSERT"));
    assertTrue(output.contains("CHANGELOG"));
    assertFalse(output.contains("-- @UNDO"));

    output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "pending_undo"));
    });
    assertTrue(output.contains("DELETE"));
    assertTrue(output.contains("CHANGELOG"));
    assertTrue(output.contains("-- @UNDO"));
  }

  @Test
  @Order(8)
  void testVersionCommand() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "version", "20080827200217"));
    });
    assertFalse(output.contains("FAILURE"));
  }

  @Test
  @Order(9)
  void testSkippedScript() throws Exception {
    testStatusContainsNoPendingMigrations();
    File skipped = new File(dir + File.separator + "scripts", "20080827200215_skipped_migration.sql");
    assertTrue(skipped.createNewFile());
    try {
      String output = SystemLambda.tapSystemOut(() -> {
        Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "up"));
      });
      assertFalse(output.contains("FAILURE"));
      assertEquals(1, TestUtil.countStr(output, "WARNING"));
      assertTrue(output.contains(
          "WARNING: Migration script '20080827200215_skipped_migration.sql' was not applied to the database."));
    } finally {
      skipped.delete();
    }
  }

  @Test
  @Order(10)
  void testMissingScript() throws Exception {
    Path original = Paths.get(dir + File.separator + "scripts", "20080827200216_create_procs.sql");
    Path renamed = Paths.get(dir + File.separator + "scripts", "20080827200216_create_procs._sql");
    assertEquals(renamed, Files.move(original, renamed, StandardCopyOption.REPLACE_EXISTING));
    try {
      String output = SystemLambda.tapSystemOut(() -> {
        Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "up"));
      });
      assertFalse(output.contains("FAILURE"), "Output contains: \n" + output);
      assertTrue(output.contains("WARNING: Missing migration script. id='20080827200216', description='create procs'."),
          "Output contains: \n" + output);
    } finally {
      assertEquals(original, Files.move(renamed, original, StandardCopyOption.REPLACE_EXISTING));
    }
  }

  @Test
  @Order(11)
  void testDownCommand() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "down"));
    });
    assertFalse(output.contains("FAILURE"));
    testStatusContainsPendingMigrations();
  }

  @Test
  @Order(12)
  void testPendingCommand() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "pending"));
    });
    assertFalse(output.contains("FAILURE"));
    testStatusContainsNoPendingMigrations();
  }

  @Test
  @Order(13)
  void testHelpCommand() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "--help"));
    });
    assertFalse(output.contains("FAILURE"));
    assertTrue(output.contains("--help"));
  }

  @Test
  @Order(14)
  void testDoScriptCommand() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "20080827200212", "20080827200214"));
    });
    assertFalse(output.contains("FAILURE"));
    assertFalse(output.contains("20080827200210"));
    assertFalse(output.contains("20080827200211"));
    assertFalse(output.contains("20080827200212"));
    assertTrue(output.contains("20080827200213"));
    assertTrue(output.contains("20080827200214"));
    assertFalse(output.contains("20080827200216"));
    assertFalse(output.contains("-- @UNDO"));

    output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "0", "20080827200211"));
    });
    assertFalse(output.contains("FAILURE"));
    assertTrue(output.contains("20080827200210"));
    assertTrue(output.contains("20080827200211"));
    assertFalse(output.contains("20080827200212"));
    assertFalse(output.contains("20080827200213"));
    assertFalse(output.contains("20080827200214"));
    assertFalse(output.contains("20080827200216"));
    assertFalse(output.contains("-- @UNDO"));
  }

  @Test
  @Order(15)
  void testUndoScriptCommand() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "20080827200216", "20080827200213"));
    });
    assertFalse(output.contains("FAILURE"));
    assertFalse(output.contains("20080827200210"));
    assertFalse(output.contains("20080827200211"));
    assertFalse(output.contains("20080827200212"));
    assertFalse(output.contains("20080827200213"));
    assertTrue(output.contains("20080827200214"));
    assertTrue(output.contains("20080827200216"));
    assertTrue(output.contains("-- @UNDO"));

    output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "20080827200211", "0"));
    });
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
  void shouldScriptCommandFailIfSameVersion() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      int exitCode = SystemLambda.catchSystemExit(() -> {
        Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "20080827200211", "20080827200211"));
      });
      assertEquals(1, exitCode);
    });
    assertTrue(output.contains("FAILURE"));
  }

  @Test
  void shouldInitTempDirectory() throws Exception {
    File basePath = TestUtil.getTempDir();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "init"));
    assertNotNull(basePath.list());
    assertEquals(4, basePath.list().length);
    File scriptPath = new File(basePath.getCanonicalPath() + File.separator + "scripts");
    assertEquals(3, scriptPath.list().length);
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "new", "test new migration"));
    assertEquals(4, scriptPath.list().length);
    assertTrue(TestUtil.deleteDirectory(basePath), "delete temp dir");
  }

  @Test
  void shouldRespectIdPattern() throws Exception {
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
    assertTrue(TestUtil.deleteDirectory(basePath), "delete temp dir");
  }

  @Test
  void useCustomTemplate() throws Exception {
    String desc = "test new migration";
    File basePath = TestUtil.getTempDir();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "init"));
    assertNotNull(basePath.list());
    assertEquals(4, basePath.list().length);
    File scriptPath = new File(basePath.getCanonicalPath() + File.separator + "scripts");
    assertEquals(3, scriptPath.list().length);

    File templatePath = File.createTempFile("customTemplate", "sql");
    try (PrintWriter writer = new PrintWriter(templatePath)) {
      writer.println("// ${description}");
    }
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
    assertTrue(TestUtil.deleteDirectory(basePath), "delete temp dir");
  }

  @Test
  void useCustomTemplateWithNoValue() throws Exception {
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
    assertTrue(TestUtil.deleteDirectory(basePath), "delete temp dir");
  }

  @Test
  void useCustomTemplateWithBadPath() throws Exception {
    System.setProperty("migrationsHome", TestUtil.getTempDir().getAbsolutePath());
    File basePath = TestUtil.getTempDir();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "init"));
    assertNotNull(basePath.list());
    assertEquals(4, basePath.list().length);
    File scriptPath = new File(basePath.getCanonicalPath() + File.separator + "scripts");
    assertEquals(3, scriptPath.list().length);

    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "new", "test new migration"));
    });
    assertEquals(4, scriptPath.list().length);
    assertTrue(output
        .contains("Your migrations configuration did not find your custom template.  Using the default template."));
    assertTrue(TestUtil.deleteDirectory(basePath), "delete temp dir");
  }

  @Test
  void shouldSuppressOutputIfQuietOptionEnabled() throws Throwable {
    System.setProperty("migrationsHome", TestUtil.getTempDir().getAbsolutePath());
    File basePath = TestUtil.getTempDir();
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "--quiet", "init"));
    });
    assertFalse(output.contains("Initializing:"));
    assertNotNull(basePath.list());
    assertTrue(TestUtil.deleteDirectory(basePath), "delete temp dir");
  }

  @Test
  void shouldColorizeSuccessOutputIfColorOptionEnabled() throws Throwable {
    System.setProperty("migrationsHome", TestUtil.getTempDir().getAbsolutePath());
    File basePath = TestUtil.getTempDir();
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "--color", "init"));
    });
    assertTrue(output.contains(ConsoleColors.GREEN + "SUCCESS"));
    assertNotNull(basePath.list());
    assertTrue(TestUtil.deleteDirectory(basePath), "delete temp dir");
  }

  @Test
  void shouldColorizeFailureOutputIfColorOptionEnabled() throws Throwable {
    System.setProperty("migrationsHome", TestUtil.getTempDir().getAbsolutePath());
    File basePath = TestUtil.getTempDir();
    String output = SystemLambda.tapSystemOut(() -> {
      int exitCode = SystemLambda.catchSystemExit(() -> {
        Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "--color", "new"));
      });
      assertEquals(1, exitCode);
    });
    assertTrue(output.contains(ConsoleColors.RED + "FAILURE"));
    assertTrue(TestUtil.deleteDirectory(basePath), "delete temp dir");
  }

  @Test
  void shouldShowErrorOnMissingChangelog() throws Throwable {
    // gh-220
    try {
      System.setProperty("migrations_changelog", "changelog1");
      String output = SystemLambda.tapSystemOut(() -> {
        Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "up", "1"));
      });
      assertFalse(output.contains("FAILURE"));

      System.setProperty("migrations_changelog", "changelog2");
      output = SystemLambda.tapSystemOut(() -> {
        int exitCode = SystemLambda.catchSystemExit(() -> {
          Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "pending"));
        });
        assertEquals(1, exitCode);
      });
      assertTrue(output.contains("FAILURE"));
      assertTrue(output.contains("Change log doesn't exist, no migrations applied.  Try running 'up' instead."));
    } finally {
      System.clearProperty("migrations_changelog");
    }
  }

  @Test
  void testInfoCommand() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("info"));
    });
    assertFalse(output.contains("null"), output);
  }

  @Test
  void testInfoWithNonExistentBasePath() throws Exception {
    File baseDir = TestUtil.getTempDir();
    assertTrue(baseDir.delete()); // remove empty dir
    assertFalse(baseDir.exists(), "directory does not exist");
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("info", "--path=" + baseDir.getAbsolutePath()));
    });
    assertFalse(output.contains("Migrations path must be a directory"), "base path not required for info");
    assertFalse(output.contains("null"), output);
  }

  @Test
  void testInitWithNonExistentBasePath() throws Exception {
    File baseDir = TestUtil.getTempDir();
    assertTrue(baseDir.delete()); // remove empty dir
    assertFalse(baseDir.exists(), "directory does not exist");
    String output = SystemLambda
        .tapSystemOut(() -> Migrator.main(TestUtil.args("init", "--path=" + baseDir.getAbsolutePath())));
    assertFalse(output.contains("Migrations path must be a directory"), output);
    assertTrue(new File(baseDir, "README").exists(), "README created");
    assertTrue(new File(baseDir, "environments").isDirectory(), "environments directory created");
    assertTrue(TestUtil.deleteDirectory(baseDir), "delete temp dir");
  }

  private void testStatusContainsPendingMigrations() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "status"));
    });
    assertFalse(output.contains("FAILURE"));
    assertTrue(output.contains("...pending..."));
  }

  private void testStatusContainsNoPendingMigrations() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "status"));
    });
    assertFalse(output.contains("FAILURE"));
    assertFalse(output.contains("...pending..."));
  }

}
