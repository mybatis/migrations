package org.apache.ibatis.migration.options;

import org.junit.Test;

import java.io.File;

import static org.apache.ibatis.migration.options.Options.*;
import static org.apache.ibatis.migration.options.OptionsParser.parse;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.internal.matchers.StringContains.containsString;

public class OptionsParserTest {
  @Test
  public void testOptions() {
    final SelectedOptions options = parse(new String[]{option(FORCE), option(TRACE), option(HELP)});

    assertTrue(options.isForce());
    assertTrue(options.isTrace());
    assertTrue(options.needsHelp());
  }

  @Test
  public void testEnvAndTemplate() {
    final String testValue = "test";
    final String[] args = {valuedOption(ENV, testValue), valuedOption(TEMPLATE, testValue)};
    final SelectedOptions options = parse(args);

    assertThat(options.getEnvironment(), equalTo(testValue));
    assertThat(options.getTemplate(), equalTo(testValue));
  }

  @Test
  public void testFileOptions() {
    final String testFileName = "test";
    final File testFile = new File(testFileName);
    final SelectedOptions expectedOptions = new SelectedOptions();

    final SelectedPaths paths = expectedOptions.getPaths();
    paths.setBasePath(testFile);
    paths.setEnvPath(testFile);
    paths.setScriptPath(testFile);
    paths.setDriverPath(testFile);

    final String[] args = {
        valuedOption(PATH, testFile.getAbsolutePath()),
        valuedOption(ENVPATH, testFile.getAbsolutePath()),
        valuedOption(SCRIPTPATH, testFile.getAbsolutePath()),
        valuedOption(DRIVERPATH, testFile.getAbsolutePath())
    };

    final SelectedOptions pathOptions = parse(args);
    checkFileOptionSet(pathOptions.getPaths().getBasePath(), testFileName);
    checkFileOptionSet(pathOptions.getPaths().getEnvPath(), testFileName);
    checkFileOptionSet(pathOptions.getPaths().getScriptPath(), testFileName);
    checkFileOptionSet(pathOptions.getPaths().getDriverPath(), testFileName);
  }

  private void checkFileOptionSet(File aFile, String expectedFileName) {
    assertThat(aFile.getName(), equalTo(expectedFileName));
  }

  @Test
  public void onlyPopulatesCommandOnce() {
    final String command = "command";
    final String ignoredCommand = "ignoredCommand";
    final String anotherIgnored = "anotherIgnored";

    final SelectedOptions options = parse(new String[]{command, ignoredCommand, anotherIgnored});

    assertThat(options.getCommand(), equalTo(command));
    assertThat(options.getParams(), containsString(ignoredCommand));
    assertThat(options.getParams(), containsString(anotherIgnored));
  }

  private String valuedOption(Options option, String aValue) {
    return option(option) + "=" + aValue;
  }

  private String option(Options option) {
    return "--" + option;
  }
}
