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
import org.apache.ibatis.migration.hook.scripts.NoOpHookScript;

/**
 * @author cbongiorno on 12/29/17.
 */
public class BasicHook implements Hook {
  protected static final NoOpHookScript NO_OP = NoOpHookScript.getInstance();

  private final HookScript beforeScript;
  private final HookScript afterScript;

  public BasicHook() {
    this(NO_OP, NO_OP);
  }

  public BasicHook(HookScript beforeScript, HookScript afterScript) {
    this.beforeScript = beforeScript == null ? NO_OP : beforeScript;
    this.afterScript = afterScript == null ? NO_OP : beforeScript;
  }

  @Override
  public void before(Map<String, Object> bindingMap) {
    beforeScript.execute(bindingMap);
  }

  @Override
  public void after(Map<String, Object> bindingMap) {
    afterScript.execute(bindingMap);
  }

  public HookScript getBeforeScript() {
    return beforeScript;
  }

  public HookScript getAfterScript() {
    return afterScript;
  }
}
