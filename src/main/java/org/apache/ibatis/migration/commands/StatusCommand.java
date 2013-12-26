package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.operations.StatusOperation;
import org.apache.ibatis.migration.options.SelectedOptions;

public final class StatusCommand extends BaseCommand {
  private StatusOperation operation;

  public StatusCommand(SelectedOptions options) {
    super(options);
  }

  public void execute(String... params) {
    operation = new StatusOperation().operate(getConnectionProvider(), getMigrationLoader(), getDatabaseOperationOption(), printStream);
  }

  public StatusOperation getOperation() {
    return operation;
  }
}
