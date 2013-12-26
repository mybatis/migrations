package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.operations.PendingOperation;
import org.apache.ibatis.migration.options.SelectedOptions;

public final class PendingCommand extends BaseCommand {
  public PendingCommand(SelectedOptions options) {
    super(options);
  }

  public void execute(String... params) {
    PendingOperation operation = new PendingOperation();
    operation.operate(getConnectionProvider(), getMigrationLoader(), getDatabaseOperationOption(), printStream);
  }
}
