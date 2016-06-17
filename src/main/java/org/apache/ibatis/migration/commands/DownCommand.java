/**
 *    Copyright 2010-2016 the original author or authors.
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

import org.apache.ibatis.migration.hook.MigrationHook;
import org.apache.ibatis.migration.operations.DownOperation;
import org.apache.ibatis.migration.options.SelectedOptions;

public final class DownCommand extends BaseCommand {
  public DownCommand(SelectedOptions options) {
    super(options);
  }

  @Override
  public void execute(String... params) {
    DownOperation operation = new DownOperation(getStepCountParameter(1, params));
    operation.operate(getConnectionProvider(), getMigrationLoader(),
        getDatabaseOperationOption(), printStream, createHook());
  }

  private MigrationHook createHook() {
    String before = environment().getHookBeforeDown();
    String beforeEach = environment().getHookBeforeEachDown();
    String afterEach = environment().getHookAfterEachDown();
    String after = environment().getHookAfterDown();
    if (before == null && beforeEach == null && afterEach == null && after == null) {
      return null;
    }
    return createFileMigrationHook(before, beforeEach, afterEach, after);
  }
}
