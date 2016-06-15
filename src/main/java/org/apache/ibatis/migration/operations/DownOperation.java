/**
 *    Copyright 2010-2016 the original author or authors.
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

public final class DownOperation extends DatabaseOperation {
  private Integer steps;

  public DownOperation() {
    this(null);
  }

  public DownOperation(Integer steps) {
    super();
    this.steps = steps;
  }

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
              stepCount = steps;
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
