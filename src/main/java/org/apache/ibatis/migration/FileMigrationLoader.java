package org.apache.ibatis.migration;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
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
          Change change = parseChangeFromFilename(filename);
          migrations.add(change);
        }
      }
    }
    return migrations;
  }

  private Change parseChangeFromFilename(String filename) {
    try {
      Change change = new Change();
      String[] parts = filename.split("\\.")[0].split("_");
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
      return change;
    } catch (Exception e) {
      throw new MigrationException("Error parsing change from file.  Cause: " + e, e);
    }
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
