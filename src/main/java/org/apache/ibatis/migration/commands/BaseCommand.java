package org.apache.ibatis.migration.commands;

import static org.apache.ibatis.migration.utils.Util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.ExternalResources;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.DataSourceConnectionProvider;
import org.apache.ibatis.migration.FileMigrationLoader;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.options.DatabaseOperationOption;
import org.apache.ibatis.migration.options.SelectedOptions;
import org.apache.ibatis.migration.options.SelectedPaths;
import org.apache.ibatis.parsing.PropertyParser;

public abstract class BaseCommand implements Command {
  private static final String DATE_FORMAT = "yyyyMMddHHmmss";

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
    copyResourceTo(resource, toFile, null);
  }

  protected void copyResourceTo(String resource, File toFile, Properties variables) {
    printStream.println("Creating: " + toFile.getName());
    try {
      LineNumberReader reader = new LineNumberReader(Resources.getResourceAsReader(this.getClass().getClassLoader(), resource));
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
    } catch (IOException e) {
      throw new MigrationException("Error copying " + resource + " to " + toFile.getAbsolutePath() + ".  Cause: " + e, e);
    }
  }

  protected void copyExternalResourceTo(String resource, File toFile) {
    printStream.println("Creating: " + toFile.getName());
    try {
      File sourceFile = new File(resource);
      ExternalResources.copyExternalResource(sourceFile, toFile);
    } catch (Exception e) {
      throw new MigrationException("Error copying " + resource + " to " + toFile.getAbsolutePath() + ".  Cause: " + e, e);
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
    return new FileMigrationLoader(paths.getScriptPath(), environmentProperties().getProperty("script_char_set"), environmentProperties());
  }

  protected DatabaseOperationOption getDatabaseOperationOption() {
    DatabaseOperationOption option = new DatabaseOperationOption();
    option.setChangelogTable(changelogTable());
    Properties props = environmentProperties();
    option.setStopOnError(!options.isForce());
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
