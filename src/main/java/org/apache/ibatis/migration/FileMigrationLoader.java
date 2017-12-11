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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.migration.utils.Util;

public class FileMigrationLoader implements MigrationLoader {
  private final File scriptsDir;

  private final String charset;

  private final Properties variables;

  public FileMigrationLoader(File scriptsDir, String charset, Properties variables) {
    super();
    this.scriptsDir = scriptsDir;
    this.charset = charset;
    this.variables = variables;
  }

  @Override
  public List<Change> getMigrations() {
    List<Change> migrations = new ArrayList<Change>();
    if (scriptsDir.isDirectory()) {
      List<String> relativePaths = listRelativePaths(scriptsDir, "", new ArrayList<String>());
      if (relativePaths == null) {
        throw new MigrationException(scriptsDir + " does not exist.");
      }
      Collections.sort(relativePaths, getRelativePathByFilenameComparator());
      for (String relativePath : relativePaths) {
        if (relativePath.endsWith(".sql") && !isSpecialFile(relativePath)) {
          Change change = parseChangeFromFilename(relativePath);
          migrations.add(change);
        }
      }
    }
    return migrations;
  }

  private Comparator<String> getRelativePathByFilenameComparator() {
    return new Comparator<String>() {
      @Override
      public int compare(String relativePath1, String relativePath2) {
        return extractFilename(relativePath1).compareTo(extractFilename(relativePath2));
      }
    };
  }

  private int getLastIndexOfSlash(String relativePath) {
    return relativePath.lastIndexOf("/") + 1;
  }

  private String extractFilename(String relativePath) {
    return relativePath.substring(getLastIndexOfSlash(relativePath));
  }

  private String extractPath(String relativePath) {
    return relativePath.substring(0, getLastIndexOfSlash(relativePath));
  }

  private List<String> listRelativePaths(File scriptsDir, String path, List<String> relativePathsAccumulator) {
    File[] files = scriptsDir.listFiles();
    for (File file : files) {
      if (file.isFile()) {
        relativePathsAccumulator.add(path + file.getName());
      } else {
        listRelativePaths(file, path + file.getName() + "/", relativePathsAccumulator);
      }
    }
    return relativePathsAccumulator;
  }

  private boolean isSpecialFile(String filename) {
    return "bootstrap.sql".equals(filename) || "onabort.sql".equals(filename);
  }

  private Change parseChangeFromFilename(String filePath) {
    try {
      Change change = new Change();
      String filename = extractFilename(filePath);
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
      change.setPath(extractPath(filePath));
      return change;
    } catch (Exception e) {
      throw new MigrationException("Error parsing change from file.  Cause: " + e, e);
    }
  }

  @Override
  public Reader getScriptReader(Change change, boolean undo) {
    try {
      return new MigrationReader(Util.file(scriptsDir, change.getFullName()), charset, undo, variables);
    } catch (IOException e) {
      throw new MigrationException("Error reading " + change.getFullName(), e);
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
        return new MigrationReader(scriptFile, charset, false, variables);
      }
      return null;
    } catch (IOException e) {
      throw new MigrationException("Error reading " + fileName, e);
    }
  }
}
