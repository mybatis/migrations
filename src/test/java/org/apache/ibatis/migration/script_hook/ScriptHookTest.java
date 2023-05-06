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
package org.apache.ibatis.migration.script_hook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.stefanbirkner.systemlambda.SystemLambda;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.ibatis.migration.Migrator;
import org.apache.ibatis.migration.io.Resources;
import org.apache.ibatis.migration.utils.TestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ScriptHookTest {

  private static File dir;

  private static final String separator = System.getProperty("line.separator");

  @BeforeAll
  public static void init() throws Exception {
    dir = Resources.getResourceAsFile("org/apache/ibatis/migration/script_hook/testdir");
  }

  @Test
  void testDoScript() throws Exception {
    System.setProperty("DB_NAME", "do_script_hook");

    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "0", "003"));
    });
    assertTrue(output.contains("SUCCESS"));

    List<String> lines = Arrays.asList(output.split(separator));
    List<Integer> lineNums = new ArrayList<>();

    lineNums.add(lineIndexOf(lines, "Before hook: false"));

    lineNums.add(lineIndexOf(lines, "Before each hook: 1/false"));
    lineNums.add(lineIndexOf(lines, "CREATE TABLE CHANGES .*"));
    lineNums.add(lineIndexOf(lines,
        "INSERT INTO CHANGES \\(ID, APPLIED_AT, DESCRIPTION\\) VALUES \\(1, '.*', 'create changelog'\\);"));
    lineNums.add(lineIndexOf(lines, "After each hook: 1/false"));

    lineNums.add(lineIndexOf(lines, "Before each hook: 2/false"));
    lineNums.add(lineIndexOf(lines, "select 'do second migration' from \\(values\\(0\\)\\);"));
    lineNums.add(lineIndexOf(lines,
        "INSERT INTO CHANGES \\(ID, APPLIED_AT, DESCRIPTION\\) VALUES \\(2, '.*', 'second migration'\\);"));
    lineNums.add(lineIndexOf(lines, "After each hook: 2/false"));

    lineNums.add(lineIndexOf(lines, "Before each hook: 3/false"));
    lineNums.add(lineIndexOf(lines, "select 'do third migration' from \\(values\\(0\\)\\);"));
    lineNums.add(lineIndexOf(lines,
        "INSERT INTO CHANGES \\(ID, APPLIED_AT, DESCRIPTION\\) VALUES \\(3, '.*', 'third migration'\\);"));
    lineNums.add(lineIndexOf(lines, "After each hook: 3/false"));

    lineNums.add(lineIndexOf(lines, "After hook: false"));

    Set<Integer> treeSet = new TreeSet<>(lineNums);
    assertEquals(treeSet.toString(), lineNums.toString());
  }

  @Test
  void testPendingDoScript() throws Exception {
    System.setProperty("DB_NAME", "pending_do_script_hook");

    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "up", "1"));
    });
    assertTrue(output.contains("SUCCESS"));

    output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "pending"));
    });
    assertTrue(output.contains("SUCCESS"));

    List<String> lines = Arrays.asList(output.split(separator));
    List<Integer> lineNums = new ArrayList<>();

    lineNums.add(lineIndexOf(lines, "Before hook: false"));

    assertEquals(-1, lineIndexOf(lines, "Before each hook: 1/false"));
    assertEquals(-1, lineIndexOf(lines, "CREATE TABLE CHANGES .*"));
    assertEquals(-1, lineIndexOf(lines,
        "INSERT INTO CHANGES \\(ID, APPLIED_AT, DESCRIPTION\\) VALUES \\(1, '.*', 'create changelog'\\);"));
    assertEquals(-1, lineIndexOf(lines, "After each hook: 1/false"));

    lineNums.add(lineIndexOf(lines, "Before each hook: 2/false"));
    lineNums.add(lineIndexOf(lines, "select 'do second migration' from \\(values\\(0\\)\\);"));
    lineNums.add(lineIndexOf(lines,
        "INSERT INTO CHANGES \\(ID, APPLIED_AT, DESCRIPTION\\) VALUES \\(2, '.*', 'second migration'\\);"));
    lineNums.add(lineIndexOf(lines, "After each hook: 2/false"));

    lineNums.add(lineIndexOf(lines, "Before each hook: 3/false"));
    lineNums.add(lineIndexOf(lines, "select 'do third migration' from \\(values\\(0\\)\\);"));
    lineNums.add(lineIndexOf(lines,
        "INSERT INTO CHANGES \\(ID, APPLIED_AT, DESCRIPTION\\) VALUES \\(3, '.*', 'third migration'\\);"));
    lineNums.add(lineIndexOf(lines, "After each hook: 3/false"));

    lineNums.add(lineIndexOf(lines, "After hook: false"));

    Set<Integer> treeSet = new TreeSet<>(lineNums);
    assertEquals(treeSet.toString(), lineNums.toString());
  }

  @Test
  void testUndoScript() throws Exception {
    System.setProperty("DB_NAME", "undo_script_hook");

    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "003", "0"));
    });
    assertTrue(output.contains("SUCCESS"));

    List<String> lines = Arrays.asList(output.split(separator));
    List<Integer> lineNums = new ArrayList<>();

    lineNums.add(lineIndexOf(lines, "Before hook: true"));

    lineNums.add(lineIndexOf(lines, "Before each hook: 3/true"));
    lineNums.add(lineIndexOf(lines, "select 'undo third migration' from \\(values\\(0\\)\\);"));
    lineNums.add(lineIndexOf(lines, "DELETE FROM CHANGES WHERE ID = 3;"));
    lineNums.add(lineIndexOf(lines, "After each hook: 3/true"));

    lineNums.add(lineIndexOf(lines, "Before each hook: 2/true"));
    lineNums.add(lineIndexOf(lines, "select 'undo second migration' from \\(values\\(0\\)\\);"));
    lineNums.add(lineIndexOf(lines, "DELETE FROM CHANGES WHERE ID = 2;"));
    lineNums.add(lineIndexOf(lines, "After each hook: 2/true"));

    lineNums.add(lineIndexOf(lines, "Before each hook: 1/true"));
    lineNums.add(lineIndexOf(lines, "DROP TABLE CHANGES;"));
    assertEquals(-1, lineIndexOf(lines, "DELETE FROM CHANGES WHERE ID = 1;"),
        "There should be no DELETE for 'create changelog' migration. See gh-201.");
    lineNums.add(lineIndexOf(lines, "After each hook: 1/true"));

    lineNums.add(lineIndexOf(lines, "After hook: true"));

    Set<Integer> treeSet = new TreeSet<>(lineNums);
    assertEquals(treeSet.toString(), lineNums.toString());
  }

  @Test
  void testPendingUndoScript() throws Exception {
    System.setProperty("DB_NAME", "pending_undo_script_hook");

    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "up", "1"));
    });
    assertTrue(output.contains("SUCCESS"));

    output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "script", "pending_undo"));
    });
    assertTrue(output.contains("SUCCESS"));

    List<String> lines = Arrays.asList(output.split(separator));
    List<Integer> lineNums = new ArrayList<>();

    lineNums.add(lineIndexOf(lines, "Before hook: true"));

    lineNums.add(lineIndexOf(lines, "Before each hook: 3/true"));
    lineNums.add(lineIndexOf(lines, "select 'undo third migration' from \\(values\\(0\\)\\);"));
    lineNums.add(lineIndexOf(lines, "DELETE FROM CHANGES WHERE ID = 3;"));
    lineNums.add(lineIndexOf(lines, "After each hook: 3/true"));

    lineNums.add(lineIndexOf(lines, "Before each hook: 2/true"));
    lineNums.add(lineIndexOf(lines, "select 'undo second migration' from \\(values\\(0\\)\\);"));
    lineNums.add(lineIndexOf(lines, "DELETE FROM CHANGES WHERE ID = 2;"));
    lineNums.add(lineIndexOf(lines, "After each hook: 2/true"));

    assertEquals(-1, lineIndexOf(lines, "Before each hook: 1/true"));
    assertEquals(-1, lineIndexOf(lines, "DROP TABLE CHANGES;"));
    assertEquals(-1, lineIndexOf(lines, "DELETE FROM CHANGES WHERE ID = 1;"));
    assertEquals(-1, lineIndexOf(lines, "After each hook: 1/true"));

    lineNums.add(lineIndexOf(lines, "After hook: true"));

    Set<Integer> treeSet = new TreeSet<>(lineNums);
    assertEquals(treeSet.toString(), lineNums.toString());
  }

  private int lineIndexOf(List<String> lines, String regex) {
    for (int i = 0; i < lines.size(); i++) {
      if (lines.get(i).matches(regex)) {
        return i;
      }
    }
    return -1;
  }
}
