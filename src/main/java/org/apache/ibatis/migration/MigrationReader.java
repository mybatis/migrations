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
package org.apache.ibatis.migration;

import java.io.File;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Properties;

public class MigrationReader extends FilterReader {

  // Note: Cannot use lineSeparator directly at this time due to tests manipulating it
  private final String lineSeparator = System.getProperty("line.separator", "\n");

  private static final String UNDO_TAG = "@UNDO";

  private boolean undo;

  private Properties variables;

  private Part part = Part.NEW_LINE;

  private VariableStatus variableStatus = VariableStatus.NOTHING;

  private char previousChar;

  private int undoIndex;

  private int afterCommentPrefixIndex;

  private int afterDoubleSlashIndex;

  private boolean inUndo;

  private final StringBuilder buffer = new StringBuilder();

  private final StringBuilder lineBuffer = new StringBuilder();

  private final VariableReplacer replacer;

  private enum Part {
    NOT_UNDO_LINE,

    NEW_LINE,

    COMMENT_PREFIX,

    AFTER_COMMENT_PREFIX,

    DOUBLE_SLASH,

    AFTER_DOUBLE_SLASH,

    UNDO_TAG,

    AFTER_UNDO_TAG
  }

  private enum VariableStatus {
    NOTHING,

    FOUND_DOLLAR,

    FOUND_OPEN_BRACE,

    FOUND_POSSIBLE_VARIABLE
  }

  public MigrationReader(File file, String charset, boolean undo, Properties variables) throws IOException {
    this(Files.newInputStream(file.toPath()), charset, undo, variables);
  }

  public MigrationReader(InputStream inputStream, String charset, boolean undo, Properties variables) {
    super(scriptFileReader(inputStream, charset));
    this.undo = undo;
    this.variables = variables;
    replacer = new VariableReplacer(this.variables);
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    if (!undo && inUndo) {
      if (buffer.length() > 0) {
        return readFromBuffer(cbuf, off, len);
      }
      return -1;
    }
    while (buffer.length() == 0) {
      int result = in.read(cbuf, off, len);
      if (result == -1) {
        if (lineBuffer.length() > 0 && (!undo || inUndo)) {
          addToBuffer(lineBuffer);
        }
        if (buffer.length() > 0) {
          break;
        }
        return -1;
      }

      for (int i = off; i < off + result; i++) {
        char c = cbuf[i];

        determinePart(c);
        searchVariable(c);

        if (c == '\r' || c == '\n' && previousChar != '\r') {
          switch (part) {
            case AFTER_UNDO_TAG:
              if (!undo) {
                // Won't read from the file anymore.
                lineBuffer.setLength(0);
                int bufferLen = buffer.length();
                if (bufferLen == 0) {
                  return -1;
                }
                return readFromBuffer(cbuf, off, len);
              }
              addToBuffer(lineBuffer.delete(afterCommentPrefixIndex, afterDoubleSlashIndex)
                  .insert(afterCommentPrefixIndex, ' '));
              inUndo = true;
              break;
            case NOT_UNDO_LINE:
              if (!undo || inUndo) {
                addToBuffer(lineBuffer);
              } else {
                lineBuffer.setLength(0);
              }
              break;
            default:
              break;
          }
          part = Part.NEW_LINE;
        } else if (c == '\n') {
          // LF after CR
          part = Part.NEW_LINE;
        } else {
          lineBuffer.append(c);
        }
        previousChar = c;
      }
    }
    return readFromBuffer(cbuf, off, len);
  }

  private void addToBuffer(StringBuilder line) {
    replaceVariables(line);
    buffer.append(line).append(lineSeparator);
    lineBuffer.setLength(0);
  }

  private void replaceVariables(StringBuilder line) {
    if (variableStatus == VariableStatus.FOUND_POSSIBLE_VARIABLE) {
      String lineBufferStr = line.toString();
      String processed = replacer.replace(lineBufferStr);
      if (!lineBufferStr.equals(processed)) {
        line.setLength(0);
        line.append(processed);
      }
    }
    variableStatus = VariableStatus.NOTHING;
  }

  private int readFromBuffer(char[] cbuf, int off, int len) {
    int bufferLen = buffer.length();
    int read = bufferLen > len ? len : bufferLen;
    buffer.getChars(0, read, cbuf, off);
    buffer.delete(0, read);
    return read;
  }

  private void determinePart(char c) {
    switch (part) {
      case NEW_LINE:
        if (inUndo) {
          part = Part.NOT_UNDO_LINE;
        } else if (c == 0x09 || c == 0x20) {
          // ignore whitespace
        } else if (c == '/' || c == '-') {
          part = Part.COMMENT_PREFIX;
        } else {
          part = Part.NOT_UNDO_LINE;
        }
        break;
      case COMMENT_PREFIX:
        if ((c == '/' || c == '-') && c == previousChar) {
          part = Part.AFTER_COMMENT_PREFIX;
          afterCommentPrefixIndex = lineBuffer.length() + 1;
        } else {
          part = Part.NOT_UNDO_LINE;
        }
        break;
      case AFTER_COMMENT_PREFIX:
        if (c == 0x09 || c == 0x20) {
          // ignore whitespace
        } else if (c == '/') {
          part = Part.DOUBLE_SLASH;
        } else {
          part = Part.NOT_UNDO_LINE;
        }
        break;
      case DOUBLE_SLASH:
        if (c == '/' && c == previousChar) {
          part = Part.AFTER_DOUBLE_SLASH;
          afterDoubleSlashIndex = lineBuffer.length() + 1;
          undoIndex = 0;
        } else {
          part = Part.NOT_UNDO_LINE;
        }
        break;
      case AFTER_DOUBLE_SLASH:
        if (c == 0x09 || c == 0x20) {
          // ignore whitespace
        } else if (c == UNDO_TAG.charAt(undoIndex)) {
          part = Part.UNDO_TAG;
          undoIndex = 1;
        } else {
          part = Part.NOT_UNDO_LINE;
        }
        break;
      case UNDO_TAG:
        if (c != UNDO_TAG.charAt(undoIndex)) {
          part = Part.NOT_UNDO_LINE;
        } else if (++undoIndex >= UNDO_TAG.length()) {
          part = Part.AFTER_UNDO_TAG;
        }
        break;
      default:
        break;
    }
  }

  private void searchVariable(char c) {
    // This is just a quick check.
    switch (variableStatus) {
      case NOTHING:
        if ((part == Part.NOT_UNDO_LINE || part == Part.AFTER_UNDO_TAG) && c == '$') {
          variableStatus = VariableStatus.FOUND_DOLLAR;
        }
        break;
      case FOUND_DOLLAR:
        variableStatus = c == '{' ? VariableStatus.FOUND_OPEN_BRACE : VariableStatus.NOTHING;
        break;
      case FOUND_OPEN_BRACE:
        if (c == '}') {
          variableStatus = VariableStatus.FOUND_POSSIBLE_VARIABLE;
        }
        break;
      default:
        break;
    }
  }

  @Override
  public int read() throws IOException {
    char[] buf = new char[1];
    int result = read(buf, 0, 1);
    return result == -1 ? -1 : (int) buf[0];
  }

  protected static Reader scriptFileReader(InputStream inputStream, String charset) {
    if (charset == null || charset.length() == 0) {
      return new InputStreamReader(inputStream);
    }
    return new InputStreamReader(inputStream, Charset.forName(charset));
  }
}
