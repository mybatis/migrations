package org.apache.ibatis.migration.commands;

import java.math.BigDecimal;

import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.operations.VersionOperation;
import org.apache.ibatis.migration.options.SelectedOptions;

public final class VersionCommand extends BaseCommand {
  public VersionCommand(SelectedOptions options) {
    super(options);
  }

  public void execute(String... params) {
    ensureParamsPassed(params);
    ensureNumericParam(params);

    VersionOperation operation = new VersionOperation(new BigDecimal(params[0]));
    operation.operate(getConnectionProvider(), getMigrationLoader(), getDatabaseOperationOption(), printStream);
  }

  private void ensureParamsPassed(String... params) {
    if (paramsEmpty(params)) {
      throw new MigrationException("No target version specified for migration.");
    }
  }

  private void ensureNumericParam(String... params) {
    try {
      new BigDecimal(params[0]);
    } catch (Exception e) {
      throw new MigrationException("The version number must be a numeric integer.  " + e, e);
    }
  }
}
