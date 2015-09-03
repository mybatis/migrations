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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.options.DatabaseOperationOption;

public final class StatusOperation extends DatabaseOperation<StatusOperation> {
  private int applied;

  private int missingScript;

  private int pending;

  private List<Change> changes;

  @Override
  public StatusOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader, DatabaseOperationOption option, PrintStream printStream) {
    if (option == null) {
      option = new DatabaseOperationOption();
    }
    println(printStream, "ID             Applied At          Description");
    println(printStream, horizontalLine("", 80));
    List<Change> migrations = migrationsLoader.getMigrations();
    if (changelogExists(connectionProvider, option)) {
      changes = mergeWithChangelog(migrations, getChangelog(connectionProvider, option));
    } else {
      changes = new ArrayList<Change>(migrations);
      pending = migrations.size();
    }
    Collections.sort(changes);
    for (Change change : changes) {
      println(printStream, change.toString());
    }
    println(printStream);
    return this;
  }
  
  private List<Change> mergeWithChangelog(List<Change> migrations, List<Change> changelog) {
    List<Change> merged = new ArrayList<Change>();
    for (Change migration : migrations) {
      int index = changelog.indexOf(migration);
      if (index > -1) {
        Change change = changelog.get(index);
        change.setFilename(migration.getFilename());
        merged.add(change);
        applied++;
      } else {
        merged.add(migration);
        pending++;
      }
    }
    for (Change change : changelog) {
      if (migrations.indexOf(change) < 0) {
        missingScript++;
        merged.add(change);
      }
    }
    return merged;
  }

  public int getAppliedCount() {
    return applied;
  }

  public int getPendingCount() {
    return pending;
  }

  public List<Change> getCurrentStatus() {
    return changes;
  }
}
