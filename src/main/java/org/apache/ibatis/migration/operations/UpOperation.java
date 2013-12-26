package org.apache.ibatis.migration.operations;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.options.DatabaseOperationOption;

public final class UpOperation extends DatabaseOperation<UpOperation> {
  private final Integer steps;

  public UpOperation() {
    super();
    this.steps = null;
  }

  public UpOperation(Integer steps) {
    super();
    this.steps = steps;
    if (steps != null && steps.intValue() < 1) {
      throw new IllegalArgumentException("step must be positive number or null.");
    }
  }

  @Override
  public UpOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader, DatabaseOperationOption option, PrintStream printStream) {
    try {
      if (option == null) {
        option = new DatabaseOperationOption();
      }

      Change lastChange = null;
      if (changelogExists(connectionProvider, option)) {
        lastChange = getLastAppliedChange(connectionProvider, option);
      }

      List<Change> migrations = migrationsLoader.getMigrations();
      Collections.sort(migrations);
      int stepCount = 0;
      for (Change change : migrations) {
        if (lastChange == null || change.getId().compareTo(lastChange.getId()) > 0) {
          println(printStream, horizontalLine("Applying: " + change.getFilename(), 80));
          ScriptRunner runner = getScriptRunner(connectionProvider, option, printStream);
          try {
            runner.runScript(migrationsLoader.getScriptReader(change, false));
          } finally {
            runner.closeConnection();
          }
          insertChangelog(change, connectionProvider, option);
          println(printStream);
          stepCount++;
          if (steps != null && stepCount >= steps) {
            break;
          }
        }
      }
      return this;
    } catch (Exception e) {
      throw new MigrationException("Error executing command.  Cause: " + e, e);
    }
  }
}
