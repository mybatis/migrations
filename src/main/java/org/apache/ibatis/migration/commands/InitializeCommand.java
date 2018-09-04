/**
 *    Copyright 2010-2018 the original author or authors.
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

import java.io.File;
import java.util.Properties;

import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.options.SelectedOptions;
import org.apache.ibatis.migration.utils.Util;

public final class InitializeCommand extends BaseCommand {
  public InitializeCommand(SelectedOptions selectedOptions) {
    super(selectedOptions);
  }

  @Override
  @SuppressWarnings("serial")
  public void execute(String... params) {
    final File basePath = paths.getBasePath();
    final File scriptPath = paths.getScriptPath();

    printStream.println("Initializing: " + basePath);

    createDirectoryIfNecessary(basePath);
    ensureDirectoryIsEmpty(basePath);

    createDirectoryIfNecessary(paths.getEnvPath());
    createDirectoryIfNecessary(scriptPath);
    createDirectoryIfNecessary(paths.getDriverPath());

    copyResourceTo("org/apache/ibatis/migration/template_README", Util.file(basePath, "README"));
    copyResourceTo("org/apache/ibatis/migration/template_environment.properties",
        new File(paths.getEnvPath(), options.getEnvironment() + ".properties"));
    copyResourceTo("org/apache/ibatis/migration/template_bootstrap.sql", Util.file(scriptPath, "bootstrap.sql"));
    copyResourceTo("org/apache/ibatis/migration/template_changelog.sql",
        Util.file(scriptPath, getNextIDAsString() + "_create_changelog.sql"));
    copyResourceTo("org/apache/ibatis/migration/template_migration.sql",
        Util.file(scriptPath, getNextIDAsString() + "_first_migration.sql"), new Properties() {
          {
            setProperty("description", "First migration.");
          }
        });
    printStream.println("Done!");
    printStream.println();
  }

  protected void ensureDirectoryIsEmpty(File path) {
    String[] list = path.list();
    if (list.length != 0) {
      for (String entry : list) {
        if (!entry.startsWith(".")) {
          throw new MigrationException("Directory must be empty (.svn etc allowed): " + path.getAbsolutePath());
        }
      }
    }
  }

  protected void createDirectoryIfNecessary(File path) {
    if (!path.exists()) {
      printStream.println("Creating: " + path.getName());
      if (!path.mkdirs()) {
        throw new MigrationException(
            "Could not create directory path for an unknown reason. Make sure you have access to the directory.");
      }
    }
  }

}
