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

public class FileMigrationHook implements MigrationHook {

  protected final HookScript beforeScript;
  protected final HookScript beforeEachScript;
  protected final HookScript afterEachScript;
  protected final HookScript afterScript;

  public FileMigrationHook(HookScript beforeScript, HookScript beforeEachScript, HookScript afterEachScript,
      HookScript afterScript) {
    this.beforeScript = beforeScript;
    this.beforeEachScript = beforeEachScript;
    this.afterEachScript = afterEachScript;
    this.afterScript = afterScript;
  }

  @Override
  public void before(Map<String, Object> bindingMap) {
    if (beforeScript != null) {
      beforeScript.execute(bindingMap);
    }
  }

  @Override
  public void beforeEach(Map<String, Object> bindingMap) {
    if (beforeEachScript != null) {
      beforeEachScript.execute(bindingMap);
    }
  }

  @Override
  public void afterEach(Map<String, Object> bindingMap) {
    if (afterEachScript != null) {
      afterEachScript.execute(bindingMap);
    }
  }

  @Override
  public void after(Map<String, Object> bindingMap) {
    if (afterScript != null) {
      afterScript.execute(bindingMap);
    }
  }
}
