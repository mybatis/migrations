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
package org.apache.ibatis.migration.hook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.stefanbirkner.systemlambda.SystemLambda;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.apache.ibatis.migration.Migrator;
import org.apache.ibatis.migration.io.Resources;
import org.apache.ibatis.migration.utils.TestUtil;
import org.apache.ibatis.migration.utils.Util;
import org.junit.jupiter.api.Test;

class NewHookTest {

  @Test
  void shouldRunNewHooks() throws Throwable {
    File basePath = initBaseDir();
    File scriptPath = new File(basePath.getCanonicalPath() + File.separator + "scripts");
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(
          TestUtil.args("--path=" + basePath.getAbsolutePath(), "--idpattern=00", "new", "create table1 JIRA-123"));
    });
    String[] scripts = scriptPath.list();
    assertEquals(4, scripts.length);
    assertTrue(output.contains("SUCCESS"));
    assertTrue(output.contains("Description is valid."));
    assertTrue(output.contains("Renamed 03_create_table1_JIRA-123.sql to 03_create_table1_JIRA123.sql"));
    assertTrue(new File(scriptPath, "03_create_table1_JIRA123.sql").exists());
    assertTrue(TestUtil.deleteDirectory(basePath), "delete test dir");
  }

  @Test
  void shouldNotCreateFileWhenBeforeHookThrowsException() throws Throwable {
    File basePath = initBaseDir();
    File scriptPath = new File(basePath.getCanonicalPath() + File.separator + "scripts");
    String output = SystemLambda.tapSystemOut(() -> {
      int exitCode = SystemLambda.catchSystemExit(() -> {
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
    File hooksDir = new File(basePath, "hooks");
    hooksDir.mkdir();
    FileInputStream srcStream = new FileInputStream(
        Resources.getResourceAsFile("org/apache/ibatis/migration/hook/testdir/hooks/NewHook.js"));
    FileOutputStream destStream = new FileOutputStream(Util.file(hooksDir, "NewHook.js"));
    try {
      FileChannel srcChannel = srcStream.getChannel();
      FileChannel destChannel = destStream.getChannel();
      srcChannel.transferTo(0, srcChannel.size(), destChannel);
    } finally {
      srcStream.close();
      destStream.close();
    }
    // Add hook settings
    File envFile = new File(basePath.getCanonicalPath() + File.separator + "environments", "development.properties");
    PrintWriter writer = new PrintWriter(
        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(envFile, true), Charset.forName("utf-8"))));
    try {
      writer.println("hook_before_new=js:NewHook.js:_function=validateDesc");
      writer.println("hook_after_new=js:NewHook.js:_function=renameFile");
    } finally {
      writer.close();
    }
    return basePath;
  }
}
