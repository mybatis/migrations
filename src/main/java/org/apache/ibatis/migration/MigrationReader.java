/**
 *    Copyright 2010-2016 the original author or authors.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.apache.ibatis.parsing.PropertyParser;

public class MigrationReader extends Reader {

  private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

  private Reader target;

  public MigrationReader(File file, String charset, boolean undo, Properties variables) throws IOException {
    this(new FileInputStream(file), charset, undo, variables);
  }

  public MigrationReader(InputStream inputStream, String charset, boolean undo, Properties variables) throws IOException {
    final Reader source = scriptFileReader(inputStream, charset);
    try {
      BufferedReader reader = new BufferedReader(source);
      StringBuilder doBuilder = new StringBuilder();
      StringBuilder undoBuilder = new StringBuilder();
      StringBuilder currentBuilder = doBuilder;
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.trim().matches("^--\\s*//.*$")) {
          if (line.contains("@UNDO")) {
            currentBuilder = undoBuilder;
          }
          line = line.replaceFirst("--\\s*//", "-- ");
        }
        currentBuilder.append(line);
        currentBuilder.append(LINE_SEPARATOR);
      }
      if (undo) {
        target = new StringReader(PropertyParser.parse(undoBuilder.toString(), variables));
      } else {
        target = new StringReader(PropertyParser.parse(doBuilder.toString(), variables));
      }
    } finally {
      source.close();
    }
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    return target.read(cbuf, off, len);
  }

  @Override
  public void close() throws IOException {
    target.close();
  }

  protected Reader scriptFileReader(InputStream inputStream, String charset) throws FileNotFoundException, UnsupportedEncodingException {
    if (charset == null || charset.length() == 0) {
      return new InputStreamReader(inputStream);
    } else {
      return new InputStreamReader(inputStream, charset);
    }
  }
}

