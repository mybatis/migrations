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
package org.apache.ibatis.migration.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Scanner;

import org.apache.ibatis.migration.io.Resources;
import org.apache.ibatis.migration.utils.TestUtil;
import org.junit.jupiter.api.Test;

class BaseCommandTest {
  @Test
  void testNonexistentResource() throws Exception {
    String resource = "org/apache/ibatis/migration/commands/NoSuchFile.sql";
    IOException e = assertThrows(IOException.class, () -> {
      Resources.getResourceAsFile(resource);
    });
    assertEquals(e.getMessage(), "Could not find resource " + resource);
  }

  @Test
  void testNonexistentFile() throws Exception {
    String srcPath = TestUtil.getTempDir().getAbsolutePath() + FileSystems.getDefault().getSeparator()
        + "NoSuchFile.sql";
    File dest = File.createTempFile("Out", ".sql");
    dest.deleteOnExit();
    NoSuchFileException e = assertThrows(NoSuchFileException.class, () -> {
      BaseCommand.copyTemplate(Path.of(srcPath).toFile(), dest, null);
    });
    assertEquals(e.getMessage(), srcPath);
  }

  @Test
  void testCopyResource() throws Exception {
    File src = Resources.getResourceAsFile("org/apache/ibatis/migration/commands/TestTemplate.sql");
    File dest = File.createTempFile("Out", ".sql");
    dest.deleteOnExit();
    BaseCommand.copyTemplate(src, dest, null);
    assertTrue(contentOf(dest).contains("// ${var}"));
  }

  @Test
  void testCopyResourceWithVariables() throws Exception {
    File src = Resources.getResourceAsFile("org/apache/ibatis/migration/commands/TestTemplate.sql");
    File dest = File.createTempFile("Out", ".sql");
    dest.deleteOnExit();
    Properties variables = new Properties();
    variables.put("var", "Some description");
    BaseCommand.copyTemplate(src, dest, variables);
    assertTrue(contentOf(dest).contains("// Some description"));
  }

  @Test
  void testExternalFile() throws Exception {
    File src = File.createTempFile("ExternalTemplate", ".sql");
    src.deleteOnExit();
    try (PrintWriter writer = new PrintWriter(src)) {
      writer.println("// ${var}");
    }

    File dest = File.createTempFile("Out", ".sql");
    dest.deleteOnExit();
    BaseCommand.copyTemplate(src, dest, null);
    assertTrue(contentOf(dest).contains("// ${var}"));
  }

  @Test
  void testExternalFileWithVariables() throws Exception {
    File src = File.createTempFile("ExternalTemplate", ".sql");
    src.deleteOnExit();
    try (PrintWriter writer = new PrintWriter(src)) {
      writer.println("// ${var}");
    }

    File dest = File.createTempFile("Out", ".sql");
    dest.deleteOnExit();
    Properties variables = new Properties();
    variables.put("var", "Some description");
    BaseCommand.copyTemplate(src, dest, variables);
    assertTrue(contentOf(dest).contains("// Some description"));
  }

  protected static String contentOf(File file) throws FileNotFoundException {
    try (Scanner scanner = new Scanner(file)) {
      return scanner.useDelimiter("\\Z").next();
    }
  }
}
