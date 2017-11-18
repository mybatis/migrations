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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MigrationReaderTest {
  private static final String charset = "utf-8";

  private String lineSeparator;

  @Before
  public void beforeEachTest() {
    lineSeparator = System.getProperty("line.separator");
    System.setProperty("line.separator", "\n");

  }

  @After
  public void afterEachTest() {
    System.setProperty("line.separator", lineSeparator);
  }

  @Test
  public void shouldReturnDoPart() throws Exception {
    // @formatter:off
    String script = "-- comment\n"
        + "do part\n"
        + "--//@UNDO\n"
        + "undo part\n";
    String result = readAsString(new MigrationReader(strToInputStream(script, charset), charset, false, null));
    assertEquals("-- comment\n"
        + "do part\n", result);
    // @formatter:on
  }

  @Test
  public void shouldReturnUndoPart() throws Exception {
    // @formatter:off
    String script = "-- comment\n"
        + "do part\n"
        + "--//@UNDO\n"
        + "undo part\n";
    String result = readAsString(new MigrationReader(strToInputStream(script, charset), charset, true, null));
    assertEquals("-- @UNDO\n"
        + "undo part\n", result);
    // @formatter:on
  }

  @Test
  public void shouldUndoCommentBeLenient() throws Exception {
    // @formatter:off
    String script = "-- comment\n"
        + "do part\n"
        + " \t --  \t //  \t@UNDO  a \n"
        + "undo part\n";
    String result = readAsString(new MigrationReader(strToInputStream(script, charset), charset, true, null));
    assertEquals(" \t --   \t@UNDO  a \n"
        + "undo part\n", result);
    // @formatter:on
  }

  @Ignore("This won't work since 3.2.2 for performance reason.")
  @Test
  public void shouldUndoCommentAllowAnyCharBeforeAtMark() throws Exception {
    // @formatter:off
    String script = "-- comment\n"
        + "do part\n"
        + "-- // b @UNDO\n"
        + "undo part\n";
    String result = readAsString(new MigrationReader(strToInputStream(script, charset), charset, true, null));
    assertEquals("--  b @UNDO\n"
        + "undo part\n", result);
    // @formatter:on
  }

  @Test
  public void shouldRequireDoubleSlashInUndoComment() throws Exception {
    // @formatter:off
    String script = "-- comment\n"
        + "do part\n"
        + "-- @UNDO\n"
        + "undo part\n";
    // @formatter:on
    String result = readAsString(new MigrationReader(strToInputStream(script, charset), charset, true, null));
    assertEquals("", result);
  }

  @Test
  public void shouldReturnAllAsDoIfUndoCommentNotFound() throws Exception {
    // @formatter:off
    String script = "-- comment\n"
        + "do part\n";
    String result = readAsString(new MigrationReader(strToInputStream(script, charset), charset, false, null));
    assertEquals("-- comment\n"
        + "do part\n", result);
    // @formatter:on
  }

  @Test
  public void shouldReturnAllAsDoIfUndoCommentNotFound_NoEndBreak() throws Exception {
    String script = "-- ";
    String result = readAsString(new MigrationReader(strToInputStream(script, charset), charset, false, null));
    assertEquals("-- \n", result);
  }

  @Test
  public void shouldReturnEmptyUndoIfUndoCommentNotFound() throws Exception {
    // @formatter:off
    String script = "-- comment\n"
        + "do part\n";
    // @formatter:on
    String result = readAsString(new MigrationReader(strToInputStream(script, charset), charset, true, null));
    assertEquals("", result);
  }

  @Test
  public void shouldReturnEmptyUndoIfUndoCommentNotFound_NoEndBreak() throws Exception {
    String script = "-- ";
    String result = readAsString(new MigrationReader(strToInputStream(script, charset), charset, true, null));
    assertEquals("", result);
  }

  @Ignore("This won't work since 3.2.2 mainly for performance reason.")
  @Test
  public void shouldRemoveFirstDoubleSlashInEveryComment_Do() throws Exception {
    // @formatter:off
    String script = "--  //  comment\n"
        + "do part\n"
        + "--//@UNDO\n"
        + "--//some comment\n"
        + "undo part\n";
    String result = readAsString(new MigrationReader(strToInputStream(script, charset), charset, false, null));
    assertEquals("--   comment\n"
        + "do part\n", result);
    // @formatter:on
  }

  @Ignore("This won't work since 3.2.2 mainly for performance reason.")
  @Test
  public void shouldRemoveFirstDoubleSlashInEveryComment_Undo() throws Exception {
    // @formatter:off
    String script = "--  //  comment\n"
        + "do part\n"
        + "--//@UNDO\n"
        + "--//some comment\n"
        + "undo part\n";
    String result = readAsString(new MigrationReader(strToInputStream(script, charset), charset, true, null));
    assertEquals("-- @UNDO\n"
        + "-- some comment\n"
        + "undo part\n", result);
    // @formatter:on
  }

  @Test
  public void shouldSecondUndoMarkerHaveNoEffect() throws Exception {
    // @formatter:off
    String script = "--  //  comment\n"
        + "do part\n"
        + "--//@UNDO\n"
        + "first undo part\n"
        + "--//@UNDO\n"
        + "second undo part\n";
    String result = readAsString(new MigrationReader(strToInputStream(script, charset), charset, true, null));
    assertEquals("-- @UNDO\n"
        + "first undo part\n"
        + "--//@UNDO\n"
        + "second undo part\n", result);
    // @formatter:on
  }

  @Test
  public void shouldReplaceVariables_Do() throws Exception {
    // @formatter:off
    String script = "do ${a} part${b} \n"
        + "-- ${a}\n"
        + "${c} \\${b}\n"
        + "--//@UNDO\n"
        + "undo part\n";
    Properties vars = new Properties();
    vars.put("a", "AAA");
    vars.put("b", "BBB");
    vars.put("c", "CCC");
    String result = readAsString(new MigrationReader(strToInputStream(script, charset), charset, false, vars));
    assertEquals("do AAA partBBB \n"
        + "-- AAA\n"
        + "CCC ${b}\n", result);
    // @formatter:on
  }

  @Test
  public void shouldReplaceVariables_Undo() throws Exception {
    // @formatter:off
    String script = "do part\n"
        + "--//@UNDO ${c}\n"
        + "undo ${a} part${b} \n"
        + "-- ${a}\n"
        + "${c} \\${b}\n";
    Properties vars = new Properties();
    vars.put("a", "AAA");
    vars.put("b", "BBB");
    vars.put("c", "CCC");
    String result = readAsString(new MigrationReader(strToInputStream(script, charset), charset, true, vars));
    assertEquals("-- @UNDO CCC\n"
        + "undo AAA partBBB \n"
        + "-- AAA\n"
        + "CCC ${b}\n", result);
    // @formatter:on
  }

  @Test
  public void shouldNormalizeLineSeparator_Do() throws Exception {
    // This is just for consistency with older versions.
    // ScriptRunner normalizes line separator anyway.
    String originalSeparator = System.getProperty("line.separator");
    System.setProperty("line.separator", "\r\n");
    try {
      // @formatter:off
      String script = "do part 1\n"
          + "do part 2\n"
          + "--//@UNDO ${c}\n"
          + "undo part 1\n"
          + "undo part 2\n";
      String result = readAsString(new MigrationReader(strToInputStream(script, charset), charset, false, null));
      assertEquals("do part 1\r\n"
          + "do part 2\r\n", result);
      // @formatter:on
    } finally {
      System.setProperty("line.separator", originalSeparator);
    }
  }

  @Test
  public void shouldNormalizeLineSeparator_Undo() throws Exception {
    // This is just for consistency with older versions.
    // ScriptRunner normalizes line separator anyway.
    String originalSeparator = System.getProperty("line.separator");
    try {
      System.setProperty("line.separator", "\r");
      // @formatter:off
      String script = "do part 1\r\n"
          + "do part 2\r\n"
          + "--//@UNDO\r\n"
          + "undo part 1\r\n"
          + "undo part 2\r\n";
      String result = readAsString(new MigrationReader(strToInputStream(script, charset), charset, true, null));
      assertEquals("-- @UNDO\r"
          + "undo part 1\r"
          + "undo part 2\r", result);
      // @formatter:on
    } finally {
      System.setProperty("line.separator", originalSeparator);
    }
  }

  @Test
  public void shouldRespectSpecifiedOffsetAndLength() throws Exception {
    String script = "abcdefghij";
    MigrationReader reader = new MigrationReader(strToInputStream(script, charset), charset, false, null);
    try {
      char[] cbuf = new char[5];
      int read = reader.read(cbuf, 1, 3);
      assertEquals(read, 3);
      assertArrayEquals(new char[] { 0, 'a', 'b', 'c', 0 }, cbuf);
    } finally {
      reader.close();
    }
  }

  private String readAsString(Reader reader) throws IOException {
    try {
      StringBuilder buffer = new StringBuilder();
      int res;
      while ((res = reader.read()) != -1) {
        buffer.append((char) res);
      }
      return buffer.toString();
    } finally {
      reader.close();
    }
  }

  private InputStream strToInputStream(String str, String charsetName) throws UnsupportedEncodingException {
    return new ByteArrayInputStream(str.getBytes(charsetName));
  }
}
