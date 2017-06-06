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
package org.apache.ibatis.migration.commands;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Scanner;

import org.apache.ibatis.io.Resources;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BaseCommandTest {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testNonexistentResource() throws Exception {
    String resource = "org/apache/ibatis/migration/commands/NoSuchFile.sql";
    expectedException.expect(IOException.class);
    expectedException.expectMessage(is("Could not find resource " + resource));

    File src = Resources.getResourceAsFile(resource);
    File dest = File.createTempFile("Out", ".sql");
    try {
      BaseCommand.copyTemplate(src, dest, null);
    } finally {
      dest.delete();
    }
  }

  @Test
  public void testNonexistentFile() throws Exception {
    String srcPath = "/tmp/NoSuchFile.sql";
    expectedException.expect(FileNotFoundException.class);
    expectedException.expectMessage(is(srcPath + " (No such file or directory)"));

    File dest = File.createTempFile("Out", ".sql");
    try {
      BaseCommand.copyTemplate(new File(srcPath), dest, null);
    } finally {
      dest.delete();
    }
  }

  @Test
  public void testCopyResource() throws Exception {
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
  public void testCopyResourceWithVariables() throws Exception {
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
  public void testExternalFile() throws Exception {
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
  public void testExternalFileWithVariables() throws Exception {
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
    String destContent = new Scanner(file).useDelimiter("\\Z").next();
    return destContent;
  }
}
