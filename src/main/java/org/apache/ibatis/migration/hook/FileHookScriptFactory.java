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
package org.apache.ibatis.migration.hook;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Properties;

import org.apache.ibatis.migration.Environment;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.options.SelectedPaths;

public class FileHookScriptFactory implements HookScriptFactory {

  protected final SelectedPaths paths;
  protected final Environment environment;
  protected final PrintStream printStream;

  public FileHookScriptFactory(SelectedPaths paths, Environment environment, PrintStream printStream) {
    this.paths = paths;
    this.environment = environment;
    this.printStream = printStream;
  }

  @Override
  public HookScript create(String hookSetting) {
    if (hookSetting == null) {
      return null;
    }
    File hooksDir = paths.getHookPath();
    if (hooksDir == null) {
      throw new MigrationException("Hooks directory must not be null.");
    }
    if (!hooksDir.exists()) {
      throw new MigrationException("Hooks directory not found : " + hooksDir.getAbsolutePath());
    }
    String[] segments = hookSetting.split(":");
    if (segments.length < 2) {
      throw new MigrationException(
          "Error creating a HookScript. Hook setting must contain 'language' and 'file name' separated by ':' (e.g. SQL:post-up.sql).");
    }
    String charset = environment.getScriptCharset();
    Properties variables = environment.getVariables();
    // First segment is language
    String scriptLang = segments[0];
    // Second segment is file
    File scriptFile = new File(hooksDir, segments[1]);
    // The rest are script dependent options
    String[] hookOptions = Arrays.copyOfRange(segments, 2, segments.length);
    if (!scriptFile.exists()) {
      throw new MigrationException("Hook script not found : " + scriptFile.getAbsolutePath());
    }
    if ("sql".equalsIgnoreCase(scriptLang)) {
      return new SqlHookScript(scriptFile, charset, hookOptions, variables, printStream);
    } else {
      // Assuming it's JSR-223.
      return new Jsr223HookScript(scriptLang, scriptFile, charset, hookOptions, paths, variables, printStream);
    }
  }
}
