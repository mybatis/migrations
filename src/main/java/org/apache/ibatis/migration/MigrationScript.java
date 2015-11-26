/**
 *    Copyright 2010-2015 the original author or authors.
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
package org.apache.ibatis.migration;


public interface MigrationScript {
  /**
   * @return ID of this migration script.<br>
   *         Newer script should have a larger ID number.
   */
  Long getId();

  /**
   * @return Short description of this migration script.
   */
  String getDescription();

  /**
   * @return SQL statement(s) executed at runtime schema upgrade.
   */
  String getUpScript();

  String getDownScript();
}
