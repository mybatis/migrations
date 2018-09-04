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

import static org.apache.ibatis.migration.utils.Util.file;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.TimeZone;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.DataSourceConnectionProvider;
import org.apache.ibatis.migration.Environment;
import org.apache.ibatis.migration.EnvironmentLoader;
import org.apache.ibatis.migration.FileMigrationLoader;
import org.apache.ibatis.migration.FileMigrationLoaderFactory;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.PropertiesEnvironmentLoader;
import org.apache.ibatis.migration.hook.FileHookScriptFactory;
import org.apache.ibatis.migration.hook.FileMigrationHook;
import org.apache.ibatis.migration.hook.HookScriptFactory;
import org.apache.ibatis.migration.hook.MigrationHook;
import org.apache.ibatis.migration.options.DatabaseOperationOption;
import org.apache.ibatis.migration.options.Options;
import org.apache.ibatis.migration.options.SelectedOptions;
import org.apache.ibatis.migration.options.SelectedPaths;
import org.apache.ibatis.migration.utils.Util;
import org.apache.ibatis.parsing.PropertyParser;

public abstract class BaseCommand implements Command {
  private static final String DATE_FORMAT = "yyyyMMddHHmmss";

  private ClassLoader driverClassLoader;

  private Environment environment;

  protected PrintStream printStream = System.out;

  protected final SelectedOptions options;

  protected final SelectedPaths paths;

  protected BaseCommand(SelectedOptions selectedOptions) {
    this.options = selectedOptions;
    this.paths = selectedOptions.getPaths();
    if (options.isQuiet()) {
      this.printStream = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
          // throw away output
        }
      });
    }
  }

  public void setDriverClassLoader(ClassLoader aDriverClassLoader) {
    driverClassLoader = aDriverClassLoader;
  }

  public void setPrintStream(PrintStream aPrintStream) {
    if (options.isQuiet()) {
      aPrintStream.println("You selected to suppress output but a PrintStream is being set");
    }
    printStream = aPrintStream;
  }

  protected boolean paramsEmpty(String... params) {
    return params == null || params.length < 1 || params[0] == null || params[0].length() < 1;
  }

  protected String changelogTable() {
    return environment().getVariables().getProperty(Environment.CHANGELOG, "CHANGELOG");
  }

  protected String getNextIDAsString() {
    try {
      // Ensure that two subsequent calls are less likely to return the same value.
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // ignore
    }
    String idPattern = options.getIdPattern();
    if (idPattern == null) {
      idPattern = Util.getPropertyOption(Options.IDPATTERN.toString().toLowerCase());
    }
    if (idPattern != null && !idPattern.isEmpty()) {
      return generatePatternedId(idPattern);
    } else {
      return generateTimestampId();
    }
  }

  private String generatePatternedId(String pattern) {
    DecimalFormat fmt = new DecimalFormat(pattern);
    List<Change> migrations = getMigrationLoader().getMigrations();
    if (migrations.size() == 0) {
      return fmt.format(1);
    }
    Change lastChange = migrations.get(migrations.size() - 1);
    try {
      long lastId = (Long) fmt.parse(lastChange.getId().toString());
      return fmt.format(++lastId);
    } catch (ParseException e) {
      throw new MigrationException(
          "Failed to parse last id '" + lastChange.getId() + "' using the specified idPattern '" + pattern + "'");
    }
  }

  private String generateTimestampId() {
    final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    final Date now = new Date();
    dateFormat.setTimeZone(TimeZone.getTimeZone(environment().getTimeZone()));
    return dateFormat.format(now);
  }

  protected void copyResourceTo(String resource, File toFile) {
    copyResourceTo(resource, toFile, null);
  }

  protected void copyResourceTo(String resource, File toFile, Properties variables) {
    printStream.println("Creating: " + toFile.getName());
    try {
      copyTemplate(Resources.getResourceAsReader(this.getClass().getClassLoader(), resource), toFile, variables);
    } catch (IOException e) {
      throw new MigrationException("Error copying " + resource + " to " + toFile.getAbsolutePath() + ".  Cause: " + e,
          e);
    }
  }

  protected void copyExternalResourceTo(String resource, File toFile, Properties variables) {
    printStream.println("Creating: " + toFile.getName());
    try {
      File sourceFile = new File(resource);
      copyTemplate(sourceFile, toFile, variables);
    } catch (Exception e) {
      throw new MigrationException("Error copying " + resource + " to " + toFile.getAbsolutePath() + ".  Cause: " + e,
          e);
    }
  }

  protected static void copyTemplate(File templateFile, File toFile, Properties variables) throws IOException {
    copyTemplate(new FileReader(templateFile), toFile, variables);
  }

  protected static void copyTemplate(Reader templateReader, File toFile, Properties variables) throws IOException {
    LineNumberReader reader = new LineNumberReader(templateReader);
    try {
      PrintWriter writer = new PrintWriter(new FileWriter(toFile));
      try {
        String line;
        while ((line = reader.readLine()) != null) {
          line = PropertyParser.parse(line, variables);
          writer.println(line);
        }
      } finally {
        writer.close();
      }
    } finally {
      reader.close();
    }
  }

  protected File environmentFile() {
    return file(paths.getEnvPath(), options.getEnvironment() + ".properties");
  }

  protected File existingEnvironmentFile() {
    File envFile = environmentFile();
    if (!envFile.exists()) {
      throw new MigrationException("Environment file missing: " + envFile.getAbsolutePath());
    }
    return envFile;
  }

  protected Environment environment() {
    if (environment != null) {
      return environment;
    }
    EnvironmentLoader envLoader = null;
    for (EnvironmentLoader found : ServiceLoader.load(EnvironmentLoader.class)) {
      if (envLoader != null) {
        throw new MigrationException("Found multiple implementations of EnvironmentLoader via SPI.");
      }
      envLoader = found;
    }
    if (envLoader == null) {
      envLoader = new PropertiesEnvironmentLoader();
    }
    environment = envLoader.load(options.getEnvironment(), paths);
    return environment;
  }

  protected int getStepCountParameter(int defaultSteps, String... params) {
    final String stringParam = params.length > 0 ? params[0] : null;
    if (stringParam == null || "".equals(stringParam)) {
      return defaultSteps;
    } else {
      try {
        return Integer.parseInt(stringParam);
      } catch (NumberFormatException e) {
        throw new MigrationException("Invalid parameter passed to command: " + params[0]);
      }
    }
  }

  protected ConnectionProvider getConnectionProvider() {
    try {
      UnpooledDataSource dataSource = new UnpooledDataSource(getDriverClassLoader(), environment().getDriver(),
          environment().getUrl(), environment().getUsername(), environment().getPassword());
      return new DataSourceConnectionProvider(dataSource);
    } catch (Exception e) {
      throw new MigrationException("Error creating ScriptRunner.  Cause: " + e, e);
    }
  }

  private ClassLoader getDriverClassLoader() {
    File localDriverPath = getCustomDriverPath();
    if (driverClassLoader != null) {
      return driverClassLoader;
    } else if (localDriverPath.exists()) {
      try {
        List<URL> urlList = new ArrayList<URL>();
        for (File file : localDriverPath.listFiles()) {
          String filename = file.getCanonicalPath();
          if (!filename.startsWith("/")) {
            filename = "/" + filename;
          }
          urlList.add(new URL("jar:file:" + filename + "!/"));
          urlList.add(new URL("file:" + filename));
        }
        URL[] urls = urlList.toArray(new URL[urlList.size()]);
        return new URLClassLoader(urls);
      } catch (Exception e) {
        throw new MigrationException("Error creating a driver ClassLoader. Cause: " + e, e);
      }
    }
    return null;
  }

  private File getCustomDriverPath() {
    String customDriverPath = environment().getDriverPath();
    if (customDriverPath != null && customDriverPath.length() > 0) {
      return new File(customDriverPath);
    } else {
      return options.getPaths().getDriverPath();
    }
  }

  protected MigrationLoader getMigrationLoader() {
    Environment env = environment();
    MigrationLoader migrationLoader = null;
    for (FileMigrationLoaderFactory factory : ServiceLoader.load(FileMigrationLoaderFactory.class)) {
      if (migrationLoader != null) {
        throw new MigrationException("Found multiple implementations of FileMigrationLoaderFactory via SPI.");
      }
      migrationLoader = factory.create(paths, env);
    }
    return migrationLoader != null ? migrationLoader
        : new FileMigrationLoader(paths.getScriptPath(), env.getScriptCharset(), env.getVariables());
  }

  protected MigrationHook createUpHook() {
    String before = environment().getHookBeforeUp();
    String beforeEach = environment().getHookBeforeEachUp();
    String afterEach = environment().getHookAfterEachUp();
    String after = environment().getHookAfterUp();
    if (before == null && beforeEach == null && afterEach == null && after == null) {
      return null;
    }
    return createFileMigrationHook(before, beforeEach, afterEach, after);
  }

  protected MigrationHook createDownHook() {
    String before = environment().getHookBeforeDown();
    String beforeEach = environment().getHookBeforeEachDown();
    String afterEach = environment().getHookAfterEachDown();
    String after = environment().getHookAfterDown();
    if (before == null && beforeEach == null && afterEach == null && after == null) {
      return null;
    }
    return createFileMigrationHook(before, beforeEach, afterEach, after);
  }

  protected MigrationHook createFileMigrationHook(String before, String beforeEach, String afterEach, String after) {
    HookScriptFactory factory = new FileHookScriptFactory(options.getPaths(), environment(), printStream);
    return new FileMigrationHook(factory.create(before), factory.create(beforeEach), factory.create(afterEach),
        factory.create(after));
  }

  protected DatabaseOperationOption getDatabaseOperationOption() {
    DatabaseOperationOption option = new DatabaseOperationOption();
    option.setChangelogTable(changelogTable());
    option.setStopOnError(!options.isForce());
    option.setThrowWarning(!options.isForce() && !environment().isIgnoreWarnings());
    option.setEscapeProcessing(false);
    option.setAutoCommit(environment().isAutoCommit());
    option.setFullLineDelimiter(environment().isFullLineDelimiter());
    option.setSendFullScript(environment().isSendFullScript());
    option.setRemoveCRs(environment().isRemoveCrs());
    option.setDelimiter(environment().getDelimiter());
    return option;
  }
}
