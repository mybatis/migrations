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
package org.apache.ibatis.migration.commands;

import java.math.BigDecimal;

import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.operations.VersionOperation;
import org.apache.ibatis.migration.options.SelectedOptions;

public final class VersionCommand extends BaseCommand {
  public VersionCommand(SelectedOptions options) {
    super(options);
  }

  @Override
  public void execute(String... params) {
    ensureParamsPassed(params);
    ensureNumericParam(params);

    VersionOperation operation = new VersionOperation(new BigDecimal(params[0]));
    operation.operate(getConnectionProvider(), getMigrationLoader(), getDatabaseOperationOption(), printStream,
        createUpHook(), createDownHook());
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
