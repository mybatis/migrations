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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class MigrationReaderTest {

  private static final String charset = StandardCharsets.UTF_8.name();

  private String lineSeparator;

  @BeforeEach
  void beforeEachTest() {
    lineSeparator = System.lineSeparator();
    System.setProperty("line.separator", "\n");

  }

  @AfterEach
  void afterEachTest() {
    System.setProperty("line.separator", lineSeparator);
  }

  @Test
  void shouldReturnDoPart() throws Exception {
    // @formatter:off
    String script = "-- comment\n"
        + "do part\n"
        + "--//@UNDO\n"
        + "undo part\n";
    // @formatter:on

    String result = null;
    try (InputStream stream = strToInputStream(script)) {
      result = readAsString(new MigrationReader(stream, charset, false, null));
    }

    // @formatter:off
    assertEquals("-- comment\n"
        + "do part\n", result);
    // @formatter:on
  }

  @Test
  void shouldReturnUndoPart() throws Exception {
    // @formatter:off
    String script = "-- comment\n"
        + "do part\n"
        + "--//@UNDO\n"
        + "undo part\n";
    // @formatter:on

    String result = null;
    try (InputStream stream = strToInputStream(script)) {
      result = readAsString(new MigrationReader(stream, charset, true, null));
    }

    // @formatter:off
    assertEquals("-- @UNDO\n"
        + "undo part\n", result);
    // @formatter:on
  }

  @Test
  void shouldReturnUndoPart_NoEndBreak() throws Exception {
    // @formatter:off
    String script = "-- comment\n"
        + "do part\n"
        + "--//@UNDO\n"
        + "undo part";
    // @formatter:on

    String result = null;
    try (InputStream stream = strToInputStream(script)) {
      result = readAsString(new MigrationReader(stream, charset, true, null));
    }

    // @formatter:off
    assertEquals("-- @UNDO\n"
        + "undo part\n", result);
    // @formatter:on
  }

  @Test
  void shouldUndoCommentBeLenient() throws Exception {
    // @formatter:off
    String script = "-- comment\n"
        + "do part\n"
        + " \t --  \t //  \t@UNDO  a \n"
        + "undo part\n";
    // @formatter:on

    String result = null;
    try (InputStream stream = strToInputStream(script)) {
      result = readAsString(new MigrationReader(stream, charset, true, null));
    }

    // @formatter:off
    assertEquals(" \t --   \t@UNDO  a \n"
        + "undo part\n", result);
    // @formatter:on
  }

  @Disabled("This won't work since 3.2.2 for performance reason.")
  @Test
  void shouldUndoCommentAllowAnyCharBeforeAtMark() throws Exception {
    // @formatter:off
    String script = "-- comment\n"
        + "do part\n"
        + "-- // b @UNDO\n"
        + "undo part\n";
    // @formatter:on

    String result = null;
    try (InputStream stream = strToInputStream(script)) {
      result = readAsString(new MigrationReader(stream, charset, true, null));
    }

    // @formatter:off
    assertEquals("--  b @UNDO\n"
        + "undo part\n", result);
    // @formatter:on
  }

  @Test
  void shouldRequireDoubleSlashInUndoComment() throws Exception {
    // @formatter:off
    String script = "-- comment\n"
        + "do part\n"
        + "-- @UNDO\n"
        + "undo part\n";
    // @formatter:on

    String result = null;
    try (InputStream stream = strToInputStream(script)) {
      result = readAsString(new MigrationReader(stream, charset, true, null));
    }

    assertEquals("", result);
  }

  @Test
  void shouldReturnAllAsDoIfUndoCommentNotFound() throws Exception {
    // @formatter:off
    String script = "-- comment\n"
        + "do part\n";
    // @formatter:on

    String result = null;
    try (InputStream stream = strToInputStream(script)) {
      result = readAsString(new MigrationReader(stream, charset, false, null));
    }

    // @formatter:off
    assertEquals("-- comment\n"
        + "do part\n", result);
    // @formatter:on
  }

  @Test
  void shouldReturnAllAsDoIfUndoCommentNotFound_NoEndBreak() throws Exception {
    String script = "-- ";

    String result = null;
    try (InputStream stream = strToInputStream(script)) {
      result = readAsString(new MigrationReader(stream, charset, false, null));
    }

    assertEquals("-- \n", result);
  }

  @Test
  void shouldReturnEmptyUndoIfUndoCommentNotFound() throws Exception {
    // @formatter:off
    String script = "-- comment\n"
        + "do part\n";
    // @formatter:on

    String result = null;
    try (InputStream stream = strToInputStream(script)) {
      result = readAsString(new MigrationReader(stream, charset, true, null));
    }

    assertEquals("", result);
  }

  @Test
  void shouldReturnEmptyUndoIfUndoCommentNotFound_NoEndBreak() throws Exception {
    String script = "-- ";

    String result = null;
    try (InputStream stream = strToInputStream(script)) {
      result = readAsString(new MigrationReader(stream, charset, true, null));
    }

    assertEquals("", result);
  }

  @Disabled("This won't work since 3.2.2 mainly for performance reason.")
  @Test
  void shouldRemoveFirstDoubleSlashInEveryComment_Do() throws Exception {
    // @formatter:off
    String script = "--  //  comment\n"
        + "do part\n"
        + "--//@UNDO\n"
        + "--//some comment\n"
        + "undo part\n";
    // @formatter:on

    String result = null;
    try (InputStream stream = strToInputStream(script)) {
      result = readAsString(new MigrationReader(stream, charset, false, null));
    }

    // @formatter:off
    assertEquals("--   comment\n"
        + "do part\n", result);
    // @formatter:on
  }

  @Disabled("This won't work since 3.2.2 mainly for performance reason.")
  @Test
  void shouldRemoveFirstDoubleSlashInEveryComment_Undo() throws Exception {
    // @formatter:off
    String script = "--  //  comment\n"
        + "do part\n"
        + "--//@UNDO\n"
        + "--//some comment\n"
        + "undo part\n";
    // @formatter:on

    String result = null;
    try (InputStream stream = strToInputStream(script)) {
      result = readAsString(new MigrationReader(stream, charset, true, null));
    }

    // @formatter:off
    assertEquals("-- @UNDO\n"
        + "-- some comment\n"
        + "undo part\n", result);
    // @formatter:on
  }

  @Test
  void shouldSecondUndoMarkerHaveNoEffect() throws Exception {
    // @formatter:off
    String script = "--  //  comment\n"
        + "do part\n"
        + "--//@UNDO\n"
        + "first undo part\n"
        + "--//@UNDO\n"
        + "second undo part\n";
    // @formatter:on

    String result = null;
    try (InputStream stream = strToInputStream(script)) {
      result = readAsString(new MigrationReader(stream, charset, true, null));
    }

    // @formatter:off
    assertEquals("-- @UNDO\n"
        + "first undo part\n"
        + "--//@UNDO\n"
        + "second undo part\n", result);
    // @formatter:on
  }

  @Test
  void shouldReplaceVariables_Do() throws Exception {
    // @formatter:off
    String script = "do ${a} part${b} \n"
        + "-- ${a}\n"
        + "${c} \\${b}\n"
        + "--//@UNDO\n"
        + "undo part\n";
    // @formatter:on

    Properties vars = new Properties();
    vars.put("a", "AAA");
    vars.put("b", "BBB");
    vars.put("c", "CCC");

    String result = null;
    try (InputStream stream = strToInputStream(script)) {
      result = readAsString(new MigrationReader(stream, charset, false, vars));
    }

    // @formatter:off
    assertEquals("do AAA partBBB \n"
        + "-- AAA\n"
        + "CCC ${b}\n", result);
    // @formatter:on
  }

  @Test
  void shouldReplaceVariables_Undo() throws Exception {
    // @formatter:off
    String script = "do part\n"
        + "--//@UNDO ${c}\n"
        + "undo ${a} part${b} \n"
        + "-- ${a}\n"
        + "${c} \\${b}";
    // @formatter:on

    Properties vars = new Properties();
    vars.put("a", "AAA");
    vars.put("b", "BBB");
    vars.put("c", "CCC");

    String result = null;
    try (InputStream stream = strToInputStream(script)) {
      result = readAsString(new MigrationReader(stream, charset, true, vars));
    }

    // @formatter:off
    assertEquals("-- @UNDO CCC\n"
        + "undo AAA partBBB \n"
        + "-- AAA\n"
        + "CCC ${b}\n", result);
    // @formatter:on
  }

  @Test
  void shouldNormalizeLineSeparator_Do() throws Exception {
    // This is just for consistency with older versions.
    // ScriptRunner normalizes line separator anyway.
    String originalSeparator = System.lineSeparator();
    System.setProperty("line.separator", "\r\n");
    try {
      // @formatter:off
      String script = "do part 1\n"
          + "do part 2\n"
          + "--//@UNDO ${c}\n"
          + "undo part 1\n"
          + "undo part 2\n";
      // @formatter:on

      String result = null;
      try (InputStream stream = strToInputStream(script)) {
        result = readAsString(new MigrationReader(stream, charset, false, null));
      }

      // @formatter:off
      assertEquals("do part 1\r\n"
          + "do part 2\r\n", result);
      // @formatter:on
    } finally {
      System.setProperty("line.separator", originalSeparator);
    }
  }

  @Test
  void shouldNormalizeLineSeparator_Undo() throws Exception {
    // This is just for consistency with older versions.
    // ScriptRunner normalizes line separator anyway.
    String originalSeparator = System.lineSeparator();
    try {
      System.setProperty("line.separator", "\r");
      // @formatter:off
      String script = "do part 1\r\n"
          + "do part 2\r\n"
          + "--//@UNDO\r\n"
          + "undo part 1\r\n"
          + "undo part 2\r\n";
      // @formatter:on

      String result = null;
      try (InputStream stream = strToInputStream(script)) {
        result = readAsString(new MigrationReader(stream, charset, true, null));
      }

      // @formatter:off
      assertEquals("-- @UNDO\r"
          + "undo part 1\r"
          + "undo part 2\r", result);
      // @formatter:on
    } finally {
      System.setProperty("line.separator", originalSeparator);
    }
  }

  @Test
  void shouldRespectSpecifiedOffsetAndLength() throws Exception {
    String script = "abcdefghij";
    String result = null;
    try (InputStream stream = strToInputStream(script);
        MigrationReader reader = new MigrationReader(stream, charset, false, null)) {
      char[] cbuf = new char[5];
      int read = reader.read(cbuf, 1, 3);
      assertEquals(3, read);
      assertArrayEquals(new char[] { 0, 'a', 'b', 'c', 0 }, cbuf);
    }
  }

  @Test
  void testReadWithBuffer() throws Exception {
    // @formatter:off
    String script = "long do part 123456789012345678901234567890\n"
        + "--//@UNDO\n"
        + "undo part\n";
    // @formatter:on

    String result = null;
    try (InputStream stream = strToInputStream(script);
        MigrationReader reader = new MigrationReader(stream, charset, false, null)) {
      StringBuilder buffer = new StringBuilder();
      char[] cbuf = new char[30];
      int res;
      while ((res = reader.read(cbuf)) != -1) {
        buffer.append(res == cbuf.length ? cbuf : Arrays.copyOf(cbuf, res));
      }
      assertEquals("long do part 123456789012345678901234567890\n", buffer.toString());
    }
  }

  @Test
  void shouldRetainLineBreakAfterDelimiter() throws Exception {
    // @formatter:off
    String script = "-- //@DELIMITER ~\n"
        + "abc\n";
    // @formatter:on

    String result = null;
    try (InputStream stream = strToInputStream(script)) {
      result = readAsString(new MigrationReader(stream, charset, false, null));
    }

    // @formatter:off
    assertEquals("-- //@DELIMITER ~\n"
        + "abc\n", result);
    // @formatter:on
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

  private InputStream strToInputStream(String str) {
    return new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
  }
}
