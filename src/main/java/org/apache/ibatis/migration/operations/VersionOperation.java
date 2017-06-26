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
    ensureVersionExists(migrationsLoader);
    Change change = getLastAppliedChange(connectionProvider, option);
    if (change == null || version.compareTo(change.getId()) > 0) {
      println(printStream, "Upgrading to: " + version);
      UpOperation up = new UpOperation(1);
      while (!version.equals(change.getId())) {
        up.operate(connectionProvider, migrationsLoader, option, printStream, upHook);
        change = getLastAppliedChange(connectionProvider, option);
      }
    } else if (version.compareTo(change.getId()) < 0) {
      println(printStream, "Downgrading to: " + version);
      DownOperation down = new DownOperation(1);
      while (!version.equals(change.getId())) {
        down.operate(connectionProvider, migrationsLoader, option, printStream, downHook);
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
