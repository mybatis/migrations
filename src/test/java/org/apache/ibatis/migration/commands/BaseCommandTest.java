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
package org.apache.ibatis.migration.commands;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.util.Properties;
import java.util.Scanner;

import org.apache.ibatis.migration.io.Resources;
import org.apache.ibatis.migration.utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

class BaseCommandTest {
  @Test
  void testNonexistentResource() throws Exception {
    String resource = "org/apache/ibatis/migration/commands/NoSuchFile.sql";
    IOException e = assertThrows(IOException.class, () -> {
      File src = Resources.getResourceAsFile(resource);
      File dest = File.createTempFile("Out", ".sql");
      try {
        BaseCommand.copyTemplate(src, dest, null);
      } finally {
        dest.delete();
      }
    });
    assertEquals(e.getMessage(), "Could not find resource " + resource);
  }

  @Test
  @EnabledOnOs({ OS.LINUX, OS.MAC })
  void testNonexistentFileLinuxMac() throws Exception {
    String srcPath = TestUtil.getTempDir().getAbsolutePath() + FileSystems.getDefault().getSeparator()
        + "NoSuchFile.sql";
    FileNotFoundException e = assertThrows(FileNotFoundException.class, () -> {
      File dest = File.createTempFile("Out", ".sql");
      try {
        BaseCommand.copyTemplate(new File(srcPath), dest, null);
      } finally {
        dest.delete();
      }
    });
    assertEquals(e.getMessage(), srcPath + " (No such file or directory)");
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  void testNonexistentFileWindows() throws Exception {
    String srcPath = TestUtil.getTempDir().getAbsolutePath() + FileSystems.getDefault().getSeparator()
        + "NoSuchFile.sql";
    FileNotFoundException e = assertThrows(FileNotFoundException.class, () -> {
      File dest = File.createTempFile("Out", ".sql");
      try {
        BaseCommand.copyTemplate(new File(srcPath), dest, null);
      } finally {
        dest.delete();
      }
    });
    assertEquals(e.getMessage(), srcPath + " (The system cannot find the file specified)");
  }

  @Test
  void testCopyResource() throws Exception {
    File src = Resources.getResourceAsFile("org/apache/ibatis/migration/commands/TestTemplate.sql");
    File dest = File.createTempFile("Out", ".sql");
    try {
      BaseCommand.copyTemplate(src, dest, null);
      assertTrue(contentOf(dest).contains("// ${var}"));
    } finally {
      dest.delete();
    }
  }

  @Test
  void testCopyResourceWithVariables() throws Exception {
    File src = Resources.getResourceAsFile("org/apache/ibatis/migration/commands/TestTemplate.sql");
    File dest = File.createTempFile("Out", ".sql");
    Properties variables = new Properties();
    variables.put("var", "Some description");
    try {
      BaseCommand.copyTemplate(src, dest, variables);
      assertTrue(contentOf(dest).contains("// Some description"));
    } finally {
      dest.delete();
    }
  }

  @Test
  void testExternalFile() throws Exception {
    File src = File.createTempFile("ExternalTemplate", ".sql");
    PrintWriter writer = new PrintWriter(src);
    writer.println("// ${var}");
    writer.close();

    File dest = File.createTempFile("Out", ".sql");
    try {
      BaseCommand.copyTemplate(src, dest, null);
      assertTrue(contentOf(dest).contains("// ${var}"));
    } finally {
      src.delete();
      dest.delete();
    }
  }

  @Test
  void testExternalFileWithVariables() throws Exception {
    File src = File.createTempFile("ExternalTemplate", ".sql");
    PrintWriter writer = new PrintWriter(src);
    writer.println("// ${var}");
    writer.close();

    File dest = File.createTempFile("Out", ".sql");
    Properties variables = new Properties();
    variables.put("var", "Some description");
    try {
      BaseCommand.copyTemplate(src, dest, variables);
      assertTrue(contentOf(dest).contains("// Some description"));
    } finally {
      src.delete();
      dest.delete();
    }
  }

  protected static String contentOf(File file) throws FileNotFoundException {
    try (Scanner scanner = new Scanner(file)) {
      String destContent = scanner.useDelimiter("\\Z").next();
      return destContent;
    }
  }
}
