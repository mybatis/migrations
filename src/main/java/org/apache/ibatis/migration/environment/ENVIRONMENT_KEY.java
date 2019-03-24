/**
 *    Copyright 2010-2019 the original author or authors.
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
package org.apache.ibatis.migration.environment;

enum ENVIRONMENT_KEY {
  time_zone,
  delimiter,
  script_char_set,
  full_line_delimiter,
  send_full_script,
  auto_commit,
  remove_crs,
  ignore_warnings,
  driver_path,
  driver,
  url,
  username,
  password,
  hook_before_up,
  hook_before_each_up,
  hook_after_each_up,
  hook_after_up,
  hook_before_down,
  hook_before_each_down,
  hook_after_each_down,
  hook_after_down,
  hook_before_new,
  hook_after_new
}
