package org.apache.ibatis.migration.operations;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.options.DatabaseOperationOption;

public final class PendingOperation extends DatabaseOperation<PendingOperation> {

  @Override
  public PendingOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader, DatabaseOperationOption option, PrintStream printStream) {
    try {
      if (option == null) {
        option = new DatabaseOperationOption();
      }
      if (!changelogExists(connectionProvider, option)) {
        throw new MigrationException("Change log doesn't exist, no migrations applied.  Try running 'up' instead.");
      }
      List<Change> pending = getPendingChanges(connectionProvider, migrationsLoader, option);
      println(printStream, "WARNING: Running pending migrations out of order can create unexpected results.");
      for (Change change : pending) {
        println(printStream, horizontalLine("Applying: " + change.getFilename(), 80));
        ScriptRunner runner = getScriptRunner(connectionProvider, option, printStream);
        try {
          runner.runScript(migrationsLoader.getScriptReader(change, false));
        } finally {
          runner.closeConnection();
        }
        insertChangelog(change, connectionProvider, option);
        println(printStream);
      }
      return this;
    } catch (Exception e) {
      throw new MigrationException("Error executing command.  Cause: " + e, e);
    }
  }

  private List<Change> getPendingChanges(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader, DatabaseOperationOption option) {
    List<Change> pending = new ArrayList<Change>();
    List<Change> migrations = migrationsLoader.getMigrations();
    List<Change> changelog = getChangelog(connectionProvider, option);
    for (Change change : migrations) {
      int index = changelog.indexOf(change);
      if (index < 0) {
        pending.add(change);
      }
    }
    Collections.sort(pending);
    return pending;
  }
}
