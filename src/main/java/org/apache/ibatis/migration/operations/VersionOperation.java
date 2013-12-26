package org.apache.ibatis.migration.operations;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.options.DatabaseOperationOption;

public final class VersionOperation extends DatabaseOperation<VersionOperation> {
  private BigDecimal version;

  public VersionOperation(BigDecimal version) {
    super();
    this.version = version;
    if (version == null) {
      throw new IllegalArgumentException("The version must be null.");
    }
  }

  @Override
  public VersionOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader, DatabaseOperationOption option, PrintStream printStream) {
    if (option == null) {
      option = new DatabaseOperationOption();
    }
    ensureVersionExists(migrationsLoader);
    Change change = getLastAppliedChange(connectionProvider, option);
    if (change == null || version.compareTo(change.getId()) > 0) {
      println(printStream, "Upgrading to: " + version);
      UpOperation up = new UpOperation(1);
      while (!version.equals(change.getId())) {
        up.operate(connectionProvider, migrationsLoader, option, printStream);
        change = getLastAppliedChange(connectionProvider, option);
      }
    } else if (version.compareTo(change.getId()) < 0) {
      println(printStream, "Downgrading to: " + version);
      DownOperation down = new DownOperation(1);
      while (!version.equals(change.getId())) {
        down.operate(connectionProvider, migrationsLoader, option, printStream);
        change = getLastAppliedChange(connectionProvider, option);
      }
    } else {
      println(printStream, "Already at version: " + version);
    }
    println(printStream);
    return this;
  }

  private void ensureVersionExists(MigrationLoader migrationsLoader) {
    List<Change> migrations = migrationsLoader.getMigrations();
    if (!migrations.contains(new Change(version))) {
      throw new MigrationException("A migration for the specified version number does not exist.");
    }
  }
}
