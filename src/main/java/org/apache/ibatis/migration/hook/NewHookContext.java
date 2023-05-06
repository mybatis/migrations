/*
 *    Copyright 2010-2023 the original author or authors.
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

/**
 * Hook context object that is available to <code>before_new</code> and <code>after_new</code> hooks.
 */
public class NewHookContext {
  private String description;
  private String filename;

  public NewHookContext(String description, String filename) {
    this.description = description;
    this.filename = filename;
  }

  /**
   * @return The specified description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return The name of the file that is created by new command.<br>
   *         In <code>before_new</code> hook, the file is not created yet.
   */
  public String getFilename() {
    return filename;
  }
}
