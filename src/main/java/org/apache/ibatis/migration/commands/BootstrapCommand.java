package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.operations.BootstrapOperation;
import org.apache.ibatis.migration.options.SelectedOptions;

public final class BootstrapCommand extends BaseCommand {
  public BootstrapCommand(SelectedOptions options) {
    super(options);
  }

  public void execute(String... params) {
    BootstrapOperation operation = new BootstrapOperation(options.isForce());
    operation.operate(getConnectionProvider(), getMigrationLoader(), getDatabaseOperationOption(), printStream);
  }
}
