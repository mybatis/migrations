/**
 *    Copyright 2010-2018 the original author or authors.
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
import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.hook.MigrationHook;
import org.apache.ibatis.migration.options.DatabaseOperationOption;

public final class VersionOperation extends DatabaseOperation {
  private BigDecimal version;

  public VersionOperation(BigDecimal version) {
    super();
    this.version = version;
    if (version == null) {
      throw new IllegalArgumentException("The version must be null.");
    }
  }

  public VersionOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader,
      DatabaseOperationOption option, PrintStream printStream) {
    return operate(connectionProvider, migrationsLoader, option, printStream, null, null);
  }

  public VersionOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader,
      DatabaseOperationOption option, PrintStream printStream, MigrationHook upHook, MigrationHook downHook) {
    if (option == null) {
      option = new DatabaseOperationOption();
    }
    List<Change> changesInDb = getChangelog(connectionProvider, option);
    List<Change> migrations = migrationsLoader.getMigrations();
    Change specified = new Change(version);
    if (!migrations.contains(specified)) {
      throw new MigrationException("A migration for the specified version number does not exist.");
    }
    Change lastChangeInDb = changesInDb.isEmpty() ? null : changesInDb.get(changesInDb.size() - 1);
    if (lastChangeInDb == null || specified.compareTo(lastChangeInDb) > 0) {
      println(printStream, "Upgrading to: " + version);
      int steps = 0;
      for (Change change : migrations) {
        if (change.compareTo(lastChangeInDb) > 0 && change.compareTo(specified) < 1) {
          steps++;
        }
      }
      new UpOperation(steps).operate(connectionProvider, migrationsLoader, option, printStream, upHook);
    } else if (specified.compareTo(lastChangeInDb) < 0) {
      println(printStream, "Downgrading to: " + version);
      int steps = 0;
      for (Change change : migrations) {
        if (change.compareTo(specified) > -1 && change.compareTo(lastChangeInDb) < 0) {
          steps++;
        }
      }
      new DownOperation(steps).operate(connectionProvider, migrationsLoader, option, printStream, downHook);
    } else {
      println(printStream, "Already at version: " + version);
    }
    println(printStream);
    return this;
  }
}
