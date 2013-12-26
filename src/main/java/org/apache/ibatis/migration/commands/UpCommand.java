package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.operations.UpOperation;
import org.apache.ibatis.migration.options.SelectedOptions;

public final class UpCommand extends BaseCommand {
  private final boolean runOneStepOnly;

  public UpCommand(SelectedOptions options) {
    this(options, false);
  }

  public UpCommand(SelectedOptions options, boolean runOneStepOnly) {
    super(options);
    this.runOneStepOnly = runOneStepOnly;
  }

  public void execute(String... params) {
    final int limit = getStepCountParameter(Integer.MAX_VALUE, params);
    UpOperation operation = new UpOperation(runOneStepOnly ? 1 : limit);
    operation.operate(getConnectionProvider(), getMigrationLoader(), getDatabaseOperationOption(), printStream);
  }
}
