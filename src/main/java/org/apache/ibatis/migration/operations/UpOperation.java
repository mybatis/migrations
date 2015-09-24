/**
 *    Copyright 2010-2015 the original author or authors.
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
        if (lastChange != null && change.getId().compareTo(lastChange.getId()) == 0 && !change.equals(lastChange)) {
          String errorMessage = String.format("Version conflict between changes [%s] and [%s], aborting and skipping: %s", lastChange.getId(), change.getId(), change.getFilename());
            println(printStream, horizontalLine(errorMessage, 80));
            throw new MigrationException(errorMessage);
        }
        if (lastChange == null || change.getId().compareTo(lastChange.getId()) > 0) {
          println(printStream, horizontalLine("Applying: " + change.getFilename(), 80));
          ScriptRunner runner = getScriptRunner(connectionProvider, option, printStream);
          try {
            runner.runScript(migrationsLoader.getScriptReader(change, false));
          } finally {
            runner.closeConnection();
          }
          insertChangelog(change, connectionProvider, option);
          lastChange = change;
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
