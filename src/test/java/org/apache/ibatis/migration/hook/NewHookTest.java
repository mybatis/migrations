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
package org.apache.ibatis.migration.hook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.apache.ibatis.migration.Migrator;
import org.apache.ibatis.migration.io.Resources;
import org.apache.ibatis.migration.utils.TestUtil;
import org.apache.ibatis.migration.utils.Util;
import org.junit.jupiter.api.Test;

import uk.org.webcompere.systemstubs.SystemStubs;

class NewHookTest {

  @Test
  void shouldRunNewHooks() throws Throwable {
    File basePath = initBaseDir();
    File scriptPath = Path.of(basePath.getCanonicalPath(), "scripts").toFile();
    String output = SystemStubs.tapSystemOut(() -> {
      Migrator.main(
          TestUtil.args("--path=" + basePath.getAbsolutePath(), "--idpattern=00", "new", "create table1 JIRA-123"));
    });
    String[] scripts = scriptPath.list();
    assertEquals(4, scripts.length);
    assertTrue(output.contains("SUCCESS"));
    assertTrue(output.contains("Description is valid."));
    assertTrue(output.contains("Renamed 03_create_table1_JIRA-123.sql to 03_create_table1_JIRA123.sql"));
    assertTrue(Files.exists(Path.of(scriptPath.getCanonicalPath(), "03_create_table1_JIRA123.sql")));
    assertTrue(TestUtil.deleteDirectory(basePath), "delete test dir");
  }

  @Test
  void shouldNotCreateFileWhenBeforeHookThrowsException() throws Throwable {
    File basePath = initBaseDir();
    File scriptPath = Path.of(basePath.getCanonicalPath(), "scripts").toFile();
    String output = SystemStubs.tapSystemOut(() -> {
      int exitCode = SystemStubs.catchSystemExit(() -> {
        Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "new", "create table1"));
      });
      assertEquals(1, exitCode);
    });
    assertTrue(output.contains("FAILURE"));
    assertEquals(3, scriptPath.list().length);
    assertTrue(TestUtil.deleteDirectory(basePath), "delete test dir");
  }

  protected File initBaseDir() throws IOException {
    File basePath = TestUtil.getTempDir();
    Migrator.main(TestUtil.args("--path=" + basePath.getAbsolutePath(), "--idpattern=00", "init"));
    // Copy hook script
    File hooksDir = Path.of(basePath.getCanonicalPath(), "hooks").toFile();
    hooksDir.mkdir();
    try (
        FileChannel srcChannel = FileChannel
            .open(Resources.getResourceAsFile("org/apache/ibatis/migration/hook/testdir/hooks/NewHook.js").toPath());
        FileChannel destChannel = FileChannel.open(Util.file(hooksDir, "NewHook.js").toPath(),
            StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
      srcChannel.transferTo(0, srcChannel.size(), destChannel);
    }
    // Add hook settings
    File envFile = Path.of(basePath.getCanonicalPath(), "environments", "development.properties").toFile();
    try (PrintWriter writer = new PrintWriter(
        Files.newBufferedWriter(envFile.toPath(), Charset.forName("utf-8"), StandardOpenOption.APPEND))) {
      writer.println("hook_before_new=js:NewHook.js:_function=validateDesc");
      writer.println("hook_after_new=js:NewHook.js:_function=renameFile");
    }
    return basePath;
  }
}
