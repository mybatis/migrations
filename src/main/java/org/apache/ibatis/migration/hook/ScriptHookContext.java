/*
 *    Copyright 2010-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.migration.hook;

import org.apache.ibatis.migration.Change;

/**
 * Hook context object that is available to <code>before_new</code> and <code>after_new</code> hooks.
 */
public class ScriptHookContext {
  private final Change change;
  private final boolean undo;

  public ScriptHookContext(Change change, boolean undo) {
    super();
    this.change = change;
    this.undo = undo;
  }

  public Change getChange() {
    return change;
  }

  public boolean isUndo() {
    return undo;
  }
}
