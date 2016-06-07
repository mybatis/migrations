/**
 * Copyright 2010-2016 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.migration.commands;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.DataSourceConnectionProvider;
import org.apache.ibatis.migration.FileMigrationLoader;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.io.ExternalResources;
import org.apache.ibatis.migration.options.DatabaseOperationOption;
import org.apache.ibatis.migration.options.Options;
import org.apache.ibatis.migration.options.SelectedOptions;
import org.apache.ibatis.migration.options.SelectedPaths;
import org.apache.ibatis.parsing.PropertyParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import static org.apache.ibatis.migration.utils.Util.file;

public abstract class BaseCommand implements Command {
  public static final String UNDO = "@UNDO";
  public static final String FILE = "@FILE:";

  private static final String DATE_FORMAT = "yyyyMMddHHmmss";

  private static final String MIGRATIONS_HOME = "MIGRATIONS_HOME";

  private static final String MIGRATIONS_HOME_PROPERTY = "migrationHome";

  private static final String MIGRATIONS_PROPERTIES = "migration.properties";

  private ClassLoader driverClassLoader;

  protected PrintStream printStream = System.out;

  protected final SelectedOptions options;

  protected final SelectedPaths paths;

  protected BaseCommand(SelectedOptions selectedOptions) {
    this.options = selectedOptions;
    this.paths = selectedOptions.getPaths();
  }

  public void setDriverClassLoader(ClassLoader aDriverClassLoader) {
    driverClassLoader = aDriverClassLoader;
  }

  public void setPrintStream(PrintStream aPrintStream) {
    printStream = aPrintStream;
  }

  protected boolean paramsEmpty(String... params) {
    return params == null || params.length < 1 || params[0] == null || params[0].length() < 1;
  }

  protected String changelogTable() {
    String changelog = environmentProperties().getProperty("changelog");
    if (changelog == null) {
      changelog = "CHANGELOG";
    }
    return changelog;
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
      try {
        idPattern = getPropertyOption(Options.IDPATTERN.toString().toLowerCase());
      } catch (FileNotFoundException e) {
        // ignore
      }
    }
    if (idPattern != null) {
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
      throw new MigrationException("Failed to parse last id '" + lastChange.getId() + "' using the specified idPattern '" + pattern + "'");
    }
  }

  private String generateTimestampId() {
    String timezone = environmentProperties().getProperty("time_zone");
    if (timezone == null) {
      timezone = "GMT+0:00";
    }
    final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    final Date now = new Date();
    dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
    return dateFormat.format(now);
  }

  protected void copyResourceTo(String resource, File toFile) {
    copyResourceTo(resource, toFile, null, null);
  }

  protected void copyResourceTo(String resource, File toFile, Properties variables) {
    copyResourceTo(resource, toFile, variables, null);
  }

  protected void copyResourceTo(String resource, File toFile, Properties variables, File fileTemplate) {
    printStream.println("Creating: " + toFile.getName());
    try {
      LineNumberReader reader = new LineNumberReader(Resources.getResourceAsReader(this.getClass().getClassLoader(), resource));
      try {
        PrintWriter writer = new PrintWriter(new FileWriter(toFile));
        try {
          String description = variables != null ?
            variables.getProperty("description", "no_description") :
            "no_description";
          writeFile(description, fileTemplate, reader, writer, variables);
        } finally {
          writer.close();
        }
      } finally {
        reader.close();
      }
    } catch (IOException e) {
      throw new MigrationException("Error copying " + resource + " to " + toFile.getAbsolutePath() + ".  Cause: " + e, e);
    }
  }

  private void writeFile(String description, File fileTemplate, BufferedReader reader, PrintWriter writer,
                         Properties variables) throws IOException {
    String line;
    boolean addFileTemplate = false;
    boolean addFileReference = false;
    String delimiterString = null;
    if (fileTemplate != null) {
      delimiterString = environmentProperties().getProperty("delimiter");
      delimiterString = (delimiterString == null ? ";" : delimiterString);
    }
    while ((line = reader.readLine()) != null) {
      if (variables != null) {
        line = PropertyParser.parse(line, variables);
      }
      if (fileTemplate != null) {
        if (line.contains(UNDO)) {
          addFileTemplate = true;
        } else if (line.contains(description)) {
          addFileReference = true;
        }
      }
      if (addFileReference && line.trim().isEmpty()) {
        writer.println(FILE + fileTemplate.getName());
        writer.println(delimiterString);
        addFileReference = false;
      } else if (addFileTemplate && line.trim().isEmpty()) {
        writeFileTemplate(fileTemplate, writer, delimiterString);
        addFileTemplate = false;
      } else {
        writer.println(line);
      }
    }
  }

  private void writeFileTemplate(File fileTemplate, PrintWriter writer, String delimiterString) throws IOException {
    String line;
    InputStreamReader inputStreamReader = null;
    try {
      inputStreamReader = new InputStreamReader(new FileInputStream(fileTemplate));
      BufferedReader fileReader = new BufferedReader(inputStreamReader);
      while ((line = fileReader.readLine()) != null) {
        writer.println(line);
      }
      writer.println(delimiterString);
    } finally {
      if (inputStreamReader != null) {
        inputStreamReader.close();
      }
    }
  }

  protected String migrationsHome() {
    String migrationsHome = System.getenv(MIGRATIONS_HOME);
    // Check if there is a system property
    if (migrationsHome == null) {
      migrationsHome = System.getProperty(MIGRATIONS_HOME_PROPERTY);
    }
    return migrationsHome;
  }

  protected void copyExternalResourceTo(String resource, File toFile, File fileTemplate) {
    printStream.println("Creating: " + toFile.getName());
    File tempFile = null;
    try {
      File sourceFile = new File(resource);
      if (fileTemplate != null) {
        tempFile = File.createTempFile(toFile.getName() + "_temp", ".tmp");
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = new BufferedReader(inputStreamReader);
        PrintWriter writer = new PrintWriter(new FileWriter(tempFile));
        try {
          inputStreamReader = new InputStreamReader(new FileInputStream(fileTemplate));
          writeFile(toFile.getName(), fileTemplate, reader, writer, null);
        } finally {
          writer.close();
          inputStreamReader.close();
        }
        sourceFile = tempFile;
      }
      ExternalResources.copyExternalResource(sourceFile, toFile);
      if (tempFile != null) {
        tempFile.delete();
      }
    } catch (Exception e) {
      throw new MigrationException("Error copying " + resource + " to " + toFile.getAbsolutePath() + ".  Cause: " + e, e);
    }
  }

  protected String getPropertyOption(String key) throws FileNotFoundException {
    String migrationsHome = migrationsHome();
    if (migrationsHome == null || migrationsHome.isEmpty()) {
      return null;
    }
    return ExternalResources.getConfiguredTemplate(migrationsHome + "/" + MIGRATIONS_PROPERTIES, key);
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

  protected Properties environmentProperties() {
    FileInputStream fileInputStream = null;
    try {
      File file = existingEnvironmentFile();
      Properties props = new Properties();
      fileInputStream = new FileInputStream(file);
      props.load(fileInputStream);
      return props;
    } catch (IOException e) {
      throw new MigrationException("Error loading environment properties.  Cause: " + e, e);
    } finally {
      if (fileInputStream != null) {
        try {
          fileInputStream.close();
        } catch (IOException e) {
          // Nothing to do here
        }
      }
    }
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
      Properties props = environmentProperties();
      String driver = props.getProperty("driver");
      String url = props.getProperty("url");
      String username = props.getProperty("username");
      String password = props.getProperty("password");

      UnpooledDataSource dataSource = new UnpooledDataSource(getDriverClassLoader(), driver, url, username, password);
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
    String customDriverPath = environmentProperties().getProperty("driver_path");
    if (customDriverPath != null && customDriverPath.length() > 0) {
      return new File(customDriverPath);
    } else {
      return options.getPaths().getDriverPath();
    }
  }

  protected MigrationLoader getMigrationLoader() {
    return new FileMigrationLoader(paths.getScriptPath(), paths.getReferencedFilesPath(),
      environmentProperties().getProperty("script_char_set"), environmentProperties());
  }

  protected DatabaseOperationOption getDatabaseOperationOption() {
    DatabaseOperationOption option = new DatabaseOperationOption();
    option.setChangelogTable(changelogTable());
    Properties props = environmentProperties();
    option.setStopOnError(!options.isForce());
    option.setThrowWarning(!options.isForce());
    option.setEscapeProcessing(false);
    option.setAutoCommit(Boolean.valueOf(props.getProperty("auto_commit")));
    option.setFullLineDelimiter(Boolean.valueOf(props.getProperty("full_line_delimiter")));
    option.setSendFullScript(Boolean.valueOf(props.getProperty("send_full_script")));
    option.setRemoveCRs(Boolean.valueOf(props.getProperty("remove_crs")));
    String delimiterString = props.getProperty("delimiter");
    option.setDelimiter(delimiterString == null ? ";" : delimiterString);
    return option;
  }
}
