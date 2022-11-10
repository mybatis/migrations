/*
 *    Copyright 2010-2022 the original author or authors.
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
package org.apache.ibatis.migration.hook;

import static org.junit.jupiter.api.Assertions.*;

import com.github.stefanbirkner.systemlambda.SystemLambda;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.ibatis.migration.Migrator;
import org.apache.ibatis.migration.io.Resources;
import org.apache.ibatis.migration.utils.TestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MigrationHookTest {

  private static File dir;
  private static Properties env;

  @BeforeAll
  public static void init() throws Exception {
    dir = Resources.getResourceAsFile("org/apache/ibatis/migration/hook/testdir");
    env = Resources
        .getResourceAsProperties("org/apache/ibatis/migration/hook/testdir/environments/development.properties");
  }

  @Test
  void testHooks() throws Exception {
    int worklogCounter = 0;
    bootstrap();
    up();
    assertChangelogIntact();
    assertWorklogRowCount(worklogCounter += 3);
    pending();
    assertWorklogRowCount(++worklogCounter);
    down();
    assertWorklogRowCount(++worklogCounter);
    versionDown();
    assertWorklogRowCount(++worklogCounter);
    versionUp();
    assertWorklogRowCount(++worklogCounter);
  }

  private void bootstrap() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      // bootstrap creates a table used in a hook script later
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "bootstrap"));
    });
    assertTrue(output.contains("SUCCESS"));
  }

  private void up() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "up"));
    });
    assertTrue(output.contains("SUCCESS"));
    // before
    assertEquals(1, TestUtil.countStr(output, "HELLO_1"));
    assertTrue(output.indexOf("HELLO_1") < output.indexOf("Applying: 001_create_changelog.sql"));
    // before each
    assertEquals(3, TestUtil.countStr(output, "FUNCTION_GLOBALVAR_LOCALVAR1_LOCALVAR2_ARG1_ARG2"));
    // after each
    assertEquals(3, TestUtil.countStr(output,
        "insert into worklog (str1, str2, str3) values ('GLOBALVAR', 'LOCALVAR1', 'LOCALVAR2')"));
    // after
    assertEquals(1, TestUtil.countStr(output, "METHOD_GLOBALVAR_LOCALVAR1_LOCALVAR2_ARG1_ARG2"));
    // assert the global variable defined and incremented in scripts
    assertEquals(1, TestUtil.countStr(output, "SCRIPT_VAR=1"));
    assertEquals(1, TestUtil.countStr(output, "SCRIPT_VAR=5"));
    assertEquals(0, TestUtil.countStr(output, "SCRIPT_VAR=6"));
  }

  private void pending() throws Exception {
    // Create 'pending' situation intentionally.
    try (Connection con = TestUtil.getConnection(env); Statement stmt = con.createStatement()) {
      stmt.execute("delete from changes where id = 2");
      stmt.execute("drop table person");

      String output = SystemLambda.tapSystemOut(() -> {
        Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "pending"));
      });
      assertTrue(output.contains("SUCCESS"));
      // before
      assertEquals(1, TestUtil.countStr(output, "HELLO_1"));
      assertEquals(1, TestUtil.countStr(output, "Applying: 002_create_person.sql"));
      // before each
      assertEquals(1, TestUtil.countStr(output, "FUNCTION_GLOBALVAR_LOCALVAR1_LOCALVAR2_ARG1_ARG2"));
      // after each
      assertEquals(1, TestUtil.countStr(output,
          "insert into worklog (str1, str2, str3) values ('GLOBALVAR', 'LOCALVAR1', 'LOCALVAR2')"));
      // after
      assertEquals(1, TestUtil.countStr(output, "METHOD_GLOBALVAR_LOCALVAR1_LOCALVAR2_ARG1_ARG2"));
    }
  }

  private void down() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "down"));
    });
    assertTrue(output.contains("SUCCESS"));
    assertEquals(1, TestUtil.countStr(output, "Undoing: 003_create_pet.sql"));
    // before
    assertEquals(1, TestUtil.countStr(output, "insert into worklog (str1) values ('3')"));
  }

  private void versionDown() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "version", "1"));
    });
    assertTrue(output.contains("SUCCESS"));
    assertEquals(1, TestUtil.countStr(output, "Downgrading to: 1"));
    assertEquals(1, TestUtil.countStr(output, "Undoing: 002_create_person.sql"));
    // before
    assertEquals(1, TestUtil.countStr(output, "insert into worklog (str1) values ('2')"));
  }

  private void versionUp() throws Exception {
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "version", "2"));
    });
    assertTrue(output.contains("SUCCESS"));
    assertEquals(1, TestUtil.countStr(output, "Upgrading to: 2"));
    // before
    assertEquals(1, TestUtil.countStr(output, "HELLO_1"));
    assertEquals(1, TestUtil.countStr(output, "Applying: 002_create_person.sql"));
    // before each
    assertEquals(1, TestUtil.countStr(output, "FUNCTION_GLOBALVAR_LOCALVAR1_LOCALVAR2_ARG1_ARG2"));
    // after each
    assertEquals(1, TestUtil.countStr(output,
        "insert into worklog (str1, str2, str3) values ('GLOBALVAR', 'LOCALVAR1', 'LOCALVAR2')"));
    // after
    assertEquals(1, TestUtil.countStr(output, "METHOD_GLOBALVAR_LOCALVAR1_LOCALVAR2_ARG1_ARG2"));
  }

  private void assertWorklogRowCount(int expectedRows) throws SQLException, ClassNotFoundException {
    try (Connection con = TestUtil.getConnection(env); Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select count(*) from worklog")) {
      assertTrue(rs.next());
      assertEquals(expectedRows, rs.getInt(1));
    }
  }

  private void assertChangelogIntact() throws SQLException, ClassNotFoundException {
    try (Connection con = TestUtil.getConnection(env); Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select count(*) from changes where description = 'bogus description'")) {
      assertTrue(rs.next());
      assertEquals(0, rs.getInt(1));
    }
  }
}
