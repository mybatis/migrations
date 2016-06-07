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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.migration.utils.Util;

public class FileMigrationLoader implements MigrationLoader {
  private final File scriptsDir;

  private final File referencedFileDir;

  private final String charset;

  private final Properties properties;

  public FileMigrationLoader(File scriptsDir, String charset, Properties properties) {
    this(scriptsDir, null, charset, properties);
  }

  public FileMigrationLoader(File scriptsDir, File referencedFileDir, String charset, Properties properties) {
    super();
    this.scriptsDir = scriptsDir;
    this.referencedFileDir = referencedFileDir;
    this.charset = charset;
    this.properties = properties;
  }

  @Override
  public List<Change> getMigrations() {
    List<Change> migrations = new ArrayList<Change>();
    if (scriptsDir.isDirectory()) {
      String[] filenames = scriptsDir.list();
      if (filenames == null) {
        throw new MigrationException(scriptsDir + " does not exist.");
      }
      Arrays.sort(filenames);
      for (String filename : filenames) {
        if (filename.endsWith(".sql") && !isSpecialFile(filename)) {
          Change change = parseChangeFromFilename(filename);
          migrations.add(change);
        }
      }
    }
    return migrations;
  }

  private boolean isSpecialFile(String filename) {
    return "bootstrap.sql".equals(filename) || "onabort.sql".equals(filename);
  }

  private Change parseChangeFromFilename(String filename) {
    try {
      Change change = new Change();
      int lastIndexOfDot = filename.lastIndexOf(".");
      String[] parts = filename.substring(0, lastIndexOfDot).split("_");
      change.setId(new BigDecimal(parts[0]));
      StringBuilder builder = new StringBuilder();
      for (int i = 1; i < parts.length; i++) {
        if (i > 1) {
          builder.append(" ");
        }
        builder.append(parts[i]);
      }
      change.setDescription(builder.toString());
      change.setFilename(filename);
      return change;
    } catch (Exception e) {
      throw new MigrationException("Error parsing change from file.  Cause: " + e, e);
    }
  }

  @Override
  public Reader getScriptReader(Change change, boolean undo) {
    try {
      return new MigrationReader(Util.file(scriptsDir, change.getFilename()), referencedFileDir, charset, undo, properties);
    } catch (IOException e) {
      throw new MigrationException("Error reading " + change.getFilename(), e);
    }
  }

  @Override
  public Reader getBootstrapReader() {
    String fileName = "bootstrap.sql";
    return getSoleScriptReader(fileName);
  }

  @Override
  public Reader getOnAbortReader() {
    String fileName = "onabort.sql";
    return getSoleScriptReader(fileName);
  }

  private Reader getSoleScriptReader(String fileName) {
    try {
      File scriptFile = Util.file(scriptsDir, fileName);
      if (scriptFile.exists()) {
        return new MigrationReader(scriptFile, referencedFileDir, charset, false, properties);
      }
      return null;
    } catch (IOException e) {
      throw new MigrationException("Error reading " + fileName, e);
    }
  }
}
