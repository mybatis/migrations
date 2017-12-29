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
package org.apache.ibatis.migration.hook;

import java.util.Map;
import org.apache.ibatis.migration.hook.scripts.HookScript;

public class FileMigrationHook extends BasicHook implements MigrationHook {

  private final HookScript beforeEachScript;
  private final HookScript afterEachScript;

  public FileMigrationHook(HookScript beforeScript, HookScript beforeEachScript, HookScript afterEachScript,
      HookScript afterScript) {
    super(beforeScript, afterScript);
    this.beforeEachScript = beforeEachScript == null ? NO_OP : beforeEachScript;
    this.afterEachScript = afterEachScript == null ? NO_OP : afterEachScript;
  }

  public FileMigrationHook(HookScript beforeScript, HookScript afterScript) {
    this(beforeScript, NO_OP, NO_OP, afterScript);
  }

  @Override
  public void beforeEach(Map<String, Object> bindingMap) {
    beforeEachScript.execute(bindingMap);
  }

  @Override
  public void afterEach(Map<String, Object> bindingMap) {
    afterEachScript.execute(bindingMap);
  }

  public HookScript getBeforeEachScript() {
    return beforeEachScript;
  }

  public HookScript getAfterEachScript() {
    return afterEachScript;
  }
}
