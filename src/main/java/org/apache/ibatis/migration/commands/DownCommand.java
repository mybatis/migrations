package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.operations.DownOperation;
import org.apache.ibatis.migration.options.SelectedOptions;

public final class DownCommand extends BaseCommand {
  public DownCommand(SelectedOptions options) {
    super(options);
  }

  public void execute(String... params) {
    DownOperation operation = new DownOperation(getStepCountParameter(1, params));
    operation.operate(getConnectionProvider(), getMigrationLoader(), getDatabaseOperationOption(), printStream);
  }
}
