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

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class MigrationReaderTest {

  @Test
  public void testOutsideFileCreation() throws Exception {
    FileMigrationLoader migrationsLoader = createMigrationsLoader();
    List<Change> migrations = migrationsLoader.getMigrations();
    BufferedReader e = new BufferedReader(migrationsLoader.getScriptReader(migrations.get(0), false));
    String line;
    boolean addedFileProperly = false;
    while((line = e.readLine()) != null) {
      if (line.contains("SELECT * FROM dual;")) {
        addedFileProperly = true;
      }
    }
    assertTrue("Did not find SELECT * FROM dual in file", addedFileProperly);
  }

  protected FileMigrationLoader createMigrationsLoader() {
    URL scriptsUrl = getClass().getClassLoader().getResource("org/apache/ibatis/migration/runtime_migration/scripts_file");
    File scriptsDir = new File(scriptsUrl.getFile());
    URL referencedFileUrl = getClass().getClassLoader().getResource("org/apache/ibatis/migration/runtime_migration/files");
    File referencedFileDir = new File(referencedFileUrl.getFile());
    Properties properties = new Properties();
    properties.setProperty("changelog", "CHANGELOG");
    FileMigrationLoader migrationsLoader = new FileMigrationLoader(scriptsDir, referencedFileDir, "utf-8", properties);
    return migrationsLoader;
  }
}
