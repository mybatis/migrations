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
package org.apache.ibatis.migration.options;

import static org.apache.ibatis.migration.options.Options.DRIVERPATH;
import static org.apache.ibatis.migration.options.Options.ENV;
import static org.apache.ibatis.migration.options.Options.ENVPATH;
import static org.apache.ibatis.migration.options.Options.FORCE;
import static org.apache.ibatis.migration.options.Options.HELP;
import static org.apache.ibatis.migration.options.Options.HOOKPATH;
import static org.apache.ibatis.migration.options.Options.SCRIPTPATH;
import static org.apache.ibatis.migration.options.Options.TEMPLATE;
import static org.apache.ibatis.migration.options.Options.TRACE;
import static org.apache.ibatis.migration.options.OptionsParser.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

class OptionsParserTest {
  @Test
  void testOptions() {
    final SelectedOptions options = parse(new String[] { option(FORCE), option(TRACE), option(HELP) });

    assertTrue(options.isForce());
    assertTrue(options.isTrace());
    assertTrue(options.needsHelp());
  }

  @Test
  void testEnvAndTemplate() {
    final String testValue = "test";
    final String[] args = { valuedOption(ENV, testValue), valuedOption(TEMPLATE, testValue),
        valuedOption(Options.IDPATTERN, testValue) };
    final SelectedOptions options = parse(args);

    assertThat(options.getEnvironment()).isEqualTo(testValue);
    assertThat(options.getTemplate()).isEqualTo(testValue);
    assertThat(options.getIdPattern()).isEqualTo(testValue);
  }

  @Test
  void testFileOptions() {
    final String testFileName = "test";
    final File testFile = new File(testFileName);
    final SelectedOptions expectedOptions = new SelectedOptions();

    final SelectedPaths paths = expectedOptions.getPaths();
    paths.setBasePath(testFile);
    paths.setEnvPath(testFile);
    paths.setScriptPath(testFile);
    paths.setDriverPath(testFile);
    paths.setHookPath(testFile);

    final String[] args = { valuedOption(Options.PATH, testFile.getAbsolutePath()),
        valuedOption(ENVPATH, testFile.getAbsolutePath()), valuedOption(SCRIPTPATH, testFile.getAbsolutePath()),
        valuedOption(DRIVERPATH, testFile.getAbsolutePath()), valuedOption(HOOKPATH, testFile.getAbsolutePath()) };

    final SelectedOptions pathOptions = parse(args);
    checkFileOptionSet(pathOptions.getPaths().getBasePath(), testFileName);
    checkFileOptionSet(pathOptions.getPaths().getEnvPath(), testFileName);
    checkFileOptionSet(pathOptions.getPaths().getScriptPath(), testFileName);
    checkFileOptionSet(pathOptions.getPaths().getDriverPath(), testFileName);
    checkFileOptionSet(pathOptions.getPaths().getHookPath(), testFileName);
  }

  private void checkFileOptionSet(File aFile, String expectedFileName) {
    assertThat(aFile).hasName(expectedFileName);
  }

  @Test
  void onlyPopulatesCommandOnce() {
    final String command = "command";
    final String ignoredCommand = "ignoredCommand";
    final String anotherIgnored = "anotherIgnored";

    final SelectedOptions options = parse(new String[] { command, ignoredCommand, anotherIgnored });

    assertThat(options.getCommand()).isEqualTo(command);
    assertThat(options.getParams()).contains(ignoredCommand);
    assertThat(options.getParams()).contains(anotherIgnored);
  }

  private String valuedOption(Options option, String aValue) {
    return option(option) + '=' + aValue;
  }

  private String option(Options option) {
    return "--" + option;
  }
}
