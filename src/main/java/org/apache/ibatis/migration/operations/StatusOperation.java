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

  private int pending;

  private List<Change> changes;

  @Override
  public StatusOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader, DatabaseOperationOption option, PrintStream printStream) {
    if (option == null) {
      option = new DatabaseOperationOption();
    }
    println(printStream, "ID             Applied At          Description");
    println(printStream, horizontalLine("", 80));
    changes = new ArrayList<Change>();
    List<Change> migrations = migrationsLoader.getMigrations();
    if (changelogExists(connectionProvider, option)) {
      List<Change> changelog = getChangelog(connectionProvider, option);
      for (Change change : migrations) {
        int index = changelog.indexOf(change);
        if (index > -1) {
          changes.add(changelog.get(index));
          applied++;
        } else {
          changes.add(change);
          pending++;
        }
      }
    } else {
      changes.addAll(migrations);
      pending = migrations.size();
    }
    Collections.sort(changes);
    for (Change change : changes) {
      println(printStream, change.toString());
    }
    println(printStream);
    return this;
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
