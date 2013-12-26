package org.apache.ibatis.migration.operations;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.jdbc.SqlRunner;
import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.options.DatabaseOperationOption;

public final class DownOperation extends DatabaseOperation<DownOperation> {
  private Integer steps;

  public DownOperation() {
    this(null);
  }

  public DownOperation(Integer steps) {
    super();
    this.steps = steps;
  }

  @Override
  public DownOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader, DatabaseOperationOption option, PrintStream printStream) {
    try {
      if (option == null) {
        option = new DatabaseOperationOption();
      }
      Change lastChange = getLastAppliedChange(connectionProvider, option);
      if (lastChange == null) {
        println(printStream, "Changelog exist, but no migration found.");
      } else {
        List<Change> migrations = migrationsLoader.getMigrations();
        Collections.sort(migrations);
        Collections.reverse(migrations);
        int stepCount = 0;
        for (Change change : migrations) {
          if (change.getId().equals(lastChange.getId())) {
            println(printStream, horizontalLine("Undoing: " + change.getFilename(), 80));
            ScriptRunner runner = getScriptRunner(connectionProvider, option, printStream);
            try {
              runner.runScript(migrationsLoader.getScriptReader(change, true));
            } finally {
              runner.closeConnection();
            }
            if (changelogExists(connectionProvider, option)) {
              deleteChange(connectionProvider, change, option);
            } else {
              println(printStream, "Changelog doesn't exist. No further migrations will be undone (normal for the last migration).");
            }
            println(printStream);
            stepCount++;
            if (steps == null || stepCount >= steps) {
              break;
            }
            lastChange = getLastAppliedChange(connectionProvider, option);
          }
        }
      }
      return this;
    } catch (Exception e) {
      throw new MigrationException("Error undoing last migration.  Cause: " + e, e);
    }
  }

  protected void deleteChange(ConnectionProvider connectionProvider, Change change, DatabaseOperationOption option) {
    SqlRunner runner = getSqlRunner(connectionProvider);
    try {
      runner.delete("delete from " + option.getChangelogTable() + " where id = ?", change.getId());
    } catch (SQLException e) {
      throw new MigrationException("Error querying last applied migration.  Cause: " + e, e);
    } finally {
      runner.closeConnection();
    }
  }
}
