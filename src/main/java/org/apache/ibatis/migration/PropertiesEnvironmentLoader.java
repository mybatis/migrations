/**
 *    Copyright 2010-2018 the original author or authors.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.ibatis.migration.options.SelectedPaths;

public class PropertiesEnvironmentLoader implements EnvironmentLoader {

  protected enum SETTING_KEY {
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

  protected static final List<String> SETTING_KEYS;

  static {
    ArrayList<String> list = new ArrayList<String>();
    SETTING_KEY[] keys = SETTING_KEY.values();
    for (SETTING_KEY key : keys) {
      list.add(key.name());
    }
    SETTING_KEYS = Collections.unmodifiableList(list);
  }

  @Override
  public Environment load(String environmentName, SelectedPaths paths) {
    return buildEnvironment(readProperties(environmentName, paths));
  }

  protected Properties readProperties(String environmentName, SelectedPaths paths) {
    File file = new File(paths.getEnvPath(), environmentName + ".properties");
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(file);
      Properties prop = new Properties();
      prop.load(inputStream);
      return prop;
    } catch (FileNotFoundException e) {
      throw new MigrationException("Environment file missing: " + file.getAbsolutePath());
    } catch (IOException e) {
      throw new MigrationException("Error loading environment properties.  Cause: " + e, e);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }

  protected Environment buildEnvironment(Properties prop) {
    // User defined variables.
    Properties variables = new Properties();
    Set<Entry<Object, Object>> entries = prop.entrySet();
    for (Entry<Object, Object> entry : entries) {
      String key = (String) entry.getKey();
      if (!SETTING_KEYS.contains(key)) {
        variables.put(key, entry.getValue());
      }
    }
    return new Environment.Builder().timeZone(prop.getProperty(SETTING_KEY.time_zone.name()))
        .delimiter(prop.getProperty(SETTING_KEY.delimiter.name()))
        .scriptCharset(prop.getProperty(SETTING_KEY.script_char_set.name(), Charset.defaultCharset().name()))
        .fullLineDelimiter(Boolean.parseBoolean(prop.getProperty(SETTING_KEY.full_line_delimiter.name())))
        .sendFullScript(Boolean.parseBoolean(prop.getProperty(SETTING_KEY.send_full_script.name())))
        .autoCommit(Boolean.parseBoolean(prop.getProperty(SETTING_KEY.auto_commit.name())))
        .removeCrs(Boolean.parseBoolean(prop.getProperty(SETTING_KEY.remove_crs.name())))
        .ignoreWarnings(Boolean.parseBoolean(prop.getProperty(SETTING_KEY.ignore_warnings.name(), "true")))
        .driverPath(prop.getProperty(SETTING_KEY.driver_path.name()))
        .driver(prop.getProperty(SETTING_KEY.driver.name())).url(prop.getProperty(SETTING_KEY.url.name()))
        .username(prop.getProperty(SETTING_KEY.username.name())).password(prop.getProperty(SETTING_KEY.password.name()))
        .hookBeforeUp(prop.getProperty(SETTING_KEY.hook_before_up.name()))
        .hookBeforeEachUp(prop.getProperty(SETTING_KEY.hook_before_each_up.name()))
        .hookAfterEachUp(prop.getProperty(SETTING_KEY.hook_after_each_up.name()))
        .hookAfterUp(prop.getProperty(SETTING_KEY.hook_after_up.name()))
        .hookBeforeDown(prop.getProperty(SETTING_KEY.hook_before_down.name()))
        .hookBeforeEachDown(prop.getProperty(SETTING_KEY.hook_before_each_down.name()))
        .hookAfterEachDown(prop.getProperty(SETTING_KEY.hook_after_each_down.name()))
        .hookAfterDown(prop.getProperty(SETTING_KEY.hook_after_down.name()))
        .hookBeforeNew(prop.getProperty(SETTING_KEY.hook_before_new.name()))
        .hookAfterNew(prop.getProperty(SETTING_KEY.hook_after_new.name())).variables(variables).build();
  }
}
