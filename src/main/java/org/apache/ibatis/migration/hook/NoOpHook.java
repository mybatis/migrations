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

/**
 * @author cbongiorno on 12/29/17.
 */
public final class NoOpHook implements MigrationHook {

  private static final NoOpHook instance = new NoOpHook();

  public static NoOpHook getInstance() {
    return instance;
  }

  private NoOpHook() {
  }

  @Override
  public void before(Map<String, Object> bindingMap) {

  }

  @Override
  public void beforeEach(Map<String, Object> bindingMap) {

  }

  @Override
  public void afterEach(Map<String, Object> bindingMap) {

  }

  @Override
  public void after(Map<String, Object> bindingMap) {

  }
}
