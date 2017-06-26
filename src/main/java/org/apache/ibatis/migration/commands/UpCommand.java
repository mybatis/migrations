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

  @Override
  public void execute(String... params) {
    final int limit = getStepCountParameter(Integer.MAX_VALUE, params);
    UpOperation operation = new UpOperation(runOneStepOnly ? 1 : limit);
    operation.operate(getConnectionProvider(), getMigrationLoader(), getDatabaseOperationOption(), printStream,
        createUpHook());
  }
}
