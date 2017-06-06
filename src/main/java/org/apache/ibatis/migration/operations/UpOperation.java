/**
 *    Copyright 2010-2017 the original author or authors.
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
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.jdbc.RuntimeSqlException;
import org.apache.ibatis.jdbc.ScriptRunner;
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

  public UpOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader,
      DatabaseOperationOption option, PrintStream printStream) {
    return operate(connectionProvider, migrationsLoader, option, printStream, null);
  }

  public UpOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader,
      DatabaseOperationOption option, PrintStream printStream, MigrationHook hook) {
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
      ScriptRunner runner = getScriptRunner(connectionProvider, option, printStream);

      Map<String, Object> hookBindings = new HashMap<String, Object>();

      Reader scriptReader = null;
      Reader onAbortScriptReader = null;
      try {
        for (Change change : migrations) {
          if (lastChange == null || change.getId().compareTo(lastChange.getId()) > 0) {
            if (stepCount == 0 && hook != null) {
              hookBindings.put(MigrationHook.HOOK_CONTEXT, new HookContext(connectionProvider, runner, null));
              hook.before(hookBindings);
            }
            if (hook != null) {
              hookBindings.put(MigrationHook.HOOK_CONTEXT, new HookContext(connectionProvider, runner, change.clone()));
              hook.beforeEach(hookBindings);
            }
            println(printStream, Util.horizontalLine("Applying: " + change.getFilename(), 80));
            scriptReader = migrationsLoader.getScriptReader(change, false);
            runner.runScript(scriptReader);
            insertChangelog(change, connectionProvider, option);
            println(printStream);
            if (hook != null) {
              hookBindings.put(MigrationHook.HOOK_CONTEXT, new HookContext(connectionProvider, runner, change.clone()));
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
        return this;
      } catch (RuntimeSqlException e) {
        onAbortScriptReader = migrationsLoader.getOnAbortReader();
        if (onAbortScriptReader != null) {
          println(printStream);
          println(printStream, Util.horizontalLine("Executing onabort.sql script.", 80));
          runner.runScript(onAbortScriptReader);
          println(printStream);
        }
        throw e;
      } finally {
        if (scriptReader != null) {
          scriptReader.close();
        }
        if (onAbortScriptReader != null) {
          onAbortScriptReader.close();
        }
        runner.closeConnection();
      }
    } catch (Throwable e) {
      while (e instanceof MigrationException) {
        e = e.getCause();
      }
      throw new MigrationException("Error executing command.  Cause: " + e, e);
    }
  }
}
