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

public class Environment {

  public static final String CHANGELOG = "changelog";

  private enum SETTING_KEY {
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
    hook_after_down
  }

  private static final List<String> SETTING_KEYS;

  static {
    ArrayList<String> list = new ArrayList<String>();
    SETTING_KEY[] keys = SETTING_KEY.values();
    for (SETTING_KEY key : keys) {
      list.add(key.name());
    }
    SETTING_KEYS = Collections.unmodifiableList(list);
  }

  private final String timeZone;
  private final String delimiter;
  private final String scriptCharset;
  private final boolean fullLineDelimiter;
  private final boolean sendFullScript;
  private final boolean autoCommit;
  private final boolean removeCrs;
  private final boolean ignoreWarnings;
  private final String driverPath;
  private final String driver;
  private final String url;
  private final String username;
  private final String password;

  private final String hookBeforeUp;
  private final String hookBeforeEachUp;
  private final String hookAfterEachUp;
  private final String hookAfterUp;
  private final String hookBeforeDown;
  private final String hookBeforeEachDown;
  private final String hookAfterEachDown;
  private final String hookAfterDown;

  private final Properties variables = new Properties();

  public Environment(File file) {
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(file);
      Properties prop = new Properties();
      prop.load(inputStream);

      this.timeZone = prop.getProperty(SETTING_KEY.time_zone.name(), "GMT+0:00");
      this.delimiter = prop.getProperty(SETTING_KEY.delimiter.name(), ";");
      this.scriptCharset = prop.getProperty(SETTING_KEY.script_char_set.name(), Charset.defaultCharset().name());
      this.fullLineDelimiter = Boolean.valueOf(prop.getProperty(SETTING_KEY.full_line_delimiter.name()));
      this.sendFullScript = Boolean.valueOf(prop.getProperty(SETTING_KEY.send_full_script.name()));
      this.autoCommit = Boolean.valueOf(prop.getProperty(SETTING_KEY.auto_commit.name()));
      this.removeCrs = Boolean.valueOf(prop.getProperty(SETTING_KEY.remove_crs.name()));
      this.ignoreWarnings = Boolean.valueOf(prop.getProperty(SETTING_KEY.ignore_warnings.name(), "true"));

      this.driverPath = prop.getProperty(SETTING_KEY.driver_path.name());
      this.driver = prop.getProperty(SETTING_KEY.driver.name());
      this.url = prop.getProperty(SETTING_KEY.url.name());
      this.username = prop.getProperty(SETTING_KEY.username.name());
      this.password = prop.getProperty(SETTING_KEY.password.name());

      this.hookBeforeUp = prop.getProperty(SETTING_KEY.hook_before_up.name());
      this.hookBeforeEachUp = prop.getProperty(SETTING_KEY.hook_before_each_up.name());
      this.hookAfterEachUp = prop.getProperty(SETTING_KEY.hook_after_each_up.name());
      this.hookAfterUp = prop.getProperty(SETTING_KEY.hook_after_up.name());
      this.hookBeforeDown = prop.getProperty(SETTING_KEY.hook_before_down.name());
      this.hookBeforeEachDown = prop.getProperty(SETTING_KEY.hook_before_each_down.name());
      this.hookAfterEachDown = prop.getProperty(SETTING_KEY.hook_after_each_down.name());
      this.hookAfterDown = prop.getProperty(SETTING_KEY.hook_after_down.name());

      // User defined variables.
      Set<Entry<Object, Object>> entries = prop.entrySet();
      for (Entry<Object, Object> entry : entries) {
        String key = (String) entry.getKey();
        if (!SETTING_KEYS.contains(key)) {
          variables.put(key, entry.getValue());
        }
      }
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

  public String getTimeZone() {
    return timeZone;
  }

  public String getDelimiter() {
    return delimiter;
  }

  public String getScriptCharset() {
    return scriptCharset;
  }

  public boolean isFullLineDelimiter() {
    return fullLineDelimiter;
  }

  public boolean isSendFullScript() {
    return sendFullScript;
  }

  public boolean isAutoCommit() {
    return autoCommit;
  }

  public boolean isRemoveCrs() {
    return removeCrs;
  }

  public boolean isIgnoreWarnings() {
    return ignoreWarnings;
  }

  public String getDriverPath() {
    return driverPath;
  }

  public String getDriver() {
    return driver;
  }

  public String getUrl() {
    return url;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getHookBeforeUp() {
    return hookBeforeUp;
  }

  public String getHookBeforeEachUp() {
    return hookBeforeEachUp;
  }

  public String getHookAfterEachUp() {
    return hookAfterEachUp;
  }

  public String getHookAfterUp() {
    return hookAfterUp;
  }

  public String getHookBeforeDown() {
    return hookBeforeDown;
  }

  public String getHookBeforeEachDown() {
    return hookBeforeEachDown;
  }

  public String getHookAfterEachDown() {
    return hookAfterEachDown;
  }

  public String getHookAfterDown() {
    return hookAfterDown;
  }

  public Properties getVariables() {
    return variables;
  }
}
