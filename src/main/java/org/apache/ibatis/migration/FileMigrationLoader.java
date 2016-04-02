/**
 *    Copyright 2010-2015 the original author or authors.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.migration.utils.Util;

public class FileMigrationLoader implements MigrationLoader {

  private final File scriptsDir;

  private final String charset;

  private final Properties properties;

  public FileMigrationLoader(File scriptsDir, String charset, Properties properties) {
    super();
    this.scriptsDir = scriptsDir;
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
        if (filename.endsWith(".sql") && !"bootstrap.sql".equals(filename)) {
          Change change = ChangeValidator.parseChangeFromFilename(filename, properties);
          ChangeValidator.validateChangeForConfiguration (change, properties);
          migrations.add(change);
        }
      }
    }
    return migrations;
  }

  @Override
  public Reader getScriptReader(Change change, boolean undo) {
    try {
      return new MigrationReader(Util.file(scriptsDir, change.getFilename()), charset, undo, properties);
    } catch (IOException e) {
      throw new MigrationException("Error reading " + change.getFilename(), e);
    }
  }

  @Override
  public Reader getBootstrapReader() {
    try {
      File bootstrap = Util.file(scriptsDir, "bootstrap.sql");
      if (bootstrap.exists()) {
        return new MigrationReader(bootstrap, charset, false, properties);
      }
      return null;
    } catch (IOException e) {
      throw new MigrationException("Error reading bootstrap.sql", e);
    }
  }

}
