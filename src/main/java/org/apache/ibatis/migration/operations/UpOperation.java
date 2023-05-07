/*
 *    Copyright 2010-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.migration.operations;

import java.io.PrintStream;
import java.io.Reader;
import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.hook.HookContext;
import org.apache.ibatis.migration.hook.MigrationHook;
import org.apache.ibatis.migration.options.DatabaseOperationOption;
import org.apache.ibatis.migration.utils.Util;

public final class UpOperation extends DatabaseOperation {
  private final Integer steps;

  public UpOperation() {
    this.steps = null;
  }

  public UpOperation(Integer steps) {
    this.steps = steps;
    if (steps != null && steps.intValue() < 1) {
      throw new IllegalArgumentException("step must be positive number or null.");
    }
  }

  public UpOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader,
      DatabaseOperationOption option, PrintStream printStream) {
    return operate(connectionProvider, migrationsLoader, option, printStream, null);
  }

  public UpOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader,
      DatabaseOperationOption option, PrintStream printStream, MigrationHook hook) {
    try (Connection con = connectionProvider.getConnection()) {
      if (option == null) {
        option = new DatabaseOperationOption();
      }

      List<Change> changesInDb = Collections.emptyList();
      if (changelogExists(con, option)) {
        changesInDb = getChangelog(con, option);
      }

      List<Change> migrations = migrationsLoader.getMigrations();
      Collections.sort(migrations);
      String skippedOrMissing = checkSkippedOrMissing(changesInDb, migrations);
      int stepCount = 0;

      Map<String, Object> hookBindings = new HashMap<>();
      ScriptRunner runner = getScriptRunner(con, option, printStream);
      try {
        for (Change change : migrations) {
          if (changesInDb.isEmpty() || change.compareTo(changesInDb.get(changesInDb.size() - 1)) > 0) {
            if (stepCount == 0 && hook != null) {
              hookBindings.put(MigrationHook.HOOK_CONTEXT, new HookContext(connectionProvider, runner, null));
              hook.before(hookBindings);
            }
            if (hook != null) {
              hookBindings.put(MigrationHook.HOOK_CONTEXT,
                  new HookContext(connectionProvider, runner, new Change(change)));
              hook.beforeEach(hookBindings);
            }
            println(printStream, Util.horizontalLine("Applying: " + change.getFilename(), 80));
            try (Reader scriptReader = migrationsLoader.getScriptReader(change, false)) {
              runner.runScript(scriptReader);
            }
            insertChangelog(change, con, option);
            println(printStream);
            if (hook != null) {
              hookBindings.put(MigrationHook.HOOK_CONTEXT,
                  new HookContext(connectionProvider, runner, new Change(change)));
              hook.afterEach(hookBindings);
            }
            stepCount++;
            if (steps != null && stepCount >= steps) {
              break;
            }
          }
        }
        if (stepCount > 0 && hook != null) {
          hookBindings.put(MigrationHook.HOOK_CONTEXT, new HookContext(connectionProvider, runner, null));
          hook.after(hookBindings);
        }
        println(printStream, skippedOrMissing);
        return this;
      } catch (Exception e) {
        try (Reader onAbortScriptReader = migrationsLoader.getOnAbortReader()) {
          if (onAbortScriptReader != null) {
            println(printStream);
            println(printStream, Util.horizontalLine("Executing onabort.sql script.", 80));
            runner.runScript(onAbortScriptReader);
            println(printStream);
          }
        }
        throw e;
      }
    } catch (Throwable e) {
      while (e instanceof MigrationException && e.getCause() != null) {
        e = e.getCause();
      }
      throw new MigrationException("Error executing command.  Cause: " + e, e);
    }
  }
}
