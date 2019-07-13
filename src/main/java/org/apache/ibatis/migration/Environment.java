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
package org.apache.ibatis.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.parsing.GenericTokenParser;

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
    hook_after_down,
    hook_before_new,
    hook_after_new
  }

  private static final List<String> SETTING_KEYS;

  static {
    ArrayList<String> list = new ArrayList<>();
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

  private final String hookBeforeNew;
  private final String hookAfterNew;

  /**
   * Prefix used to lookup environment variable or system property.
   */
  private static final String PREFIX = "MIGRATIONS_";
  private final Map<String, String> envVars = System.getenv();
  private final Properties sysProps = System.getProperties();
  private final Properties variables = new Properties();

  private final GenericTokenParser parser = new GenericTokenParser("${", "}", key -> {
    String value = sysProps.getProperty(key);
    if (value == null) {
      value = envVars.get(key);
    }
    if (value == null) {
      value = "${" + key + "}";
    }
    return value;
  });

  public Environment(File file) {
    try (FileInputStream inputStream = new FileInputStream(file)) {
      Properties prop = new Properties();
      prop.load(inputStream);

      this.timeZone = readProperty(prop, SETTING_KEY.time_zone.name(), "GMT+0:00");
      this.delimiter = readProperty(prop, SETTING_KEY.delimiter.name(), ";");
      this.scriptCharset = readProperty(prop, SETTING_KEY.script_char_set.name(), Charset.defaultCharset().name());
      this.fullLineDelimiter = Boolean.valueOf(readProperty(prop, SETTING_KEY.full_line_delimiter.name()));
      this.sendFullScript = Boolean.valueOf(readProperty(prop, SETTING_KEY.send_full_script.name()));
      this.autoCommit = Boolean.valueOf(readProperty(prop, SETTING_KEY.auto_commit.name()));
      this.removeCrs = Boolean.valueOf(readProperty(prop, SETTING_KEY.remove_crs.name()));
      this.ignoreWarnings = Boolean.valueOf(readProperty(prop, SETTING_KEY.ignore_warnings.name(), "true"));

      this.driverPath = readProperty(prop, SETTING_KEY.driver_path.name());
      this.driver = readProperty(prop, SETTING_KEY.driver.name());
      this.url = readProperty(prop, SETTING_KEY.url.name());
      this.username = readProperty(prop, SETTING_KEY.username.name());
      this.password = readProperty(prop, SETTING_KEY.password.name());

      this.hookBeforeUp = readProperty(prop, SETTING_KEY.hook_before_up.name());
      this.hookBeforeEachUp = readProperty(prop, SETTING_KEY.hook_before_each_up.name());
      this.hookAfterEachUp = readProperty(prop, SETTING_KEY.hook_after_each_up.name());
      this.hookAfterUp = readProperty(prop, SETTING_KEY.hook_after_up.name());
      this.hookBeforeDown = readProperty(prop, SETTING_KEY.hook_before_down.name());
      this.hookBeforeEachDown = readProperty(prop, SETTING_KEY.hook_before_each_down.name());
      this.hookAfterEachDown = readProperty(prop, SETTING_KEY.hook_after_each_down.name());
      this.hookAfterDown = readProperty(prop, SETTING_KEY.hook_after_down.name());

      this.hookBeforeNew = readProperty(prop, SETTING_KEY.hook_before_new.name());
      this.hookAfterNew = readProperty(prop, SETTING_KEY.hook_after_new.name());

      // User defined variables.
      prop.entrySet().stream().filter(e -> !SETTING_KEYS.contains(e.getKey())).forEach(e -> {
        variables.put(e.getKey(), parser.parse((String) e.getValue()));
      });
    } catch (FileNotFoundException e) {
      throw new MigrationException("Environment file missing: " + file.getAbsolutePath());
    } catch (IOException e) {
      throw new MigrationException("Error loading environment properties.  Cause: " + e, e);
    }
  }

  protected String readProperty(Properties properties, String propertyKey) {
    return readProperty(properties, propertyKey, null);
  }

  protected String readProperty(Properties properties, String propertyKey, String defaultValue) {
    // 1. Check system properties with prefix.
    String property = sysProps.getProperty(PREFIX + propertyKey.toUpperCase(Locale.ENGLISH));
    if (property != null) {
      return property;
    }
    // 2. Check environment variables with prefix.
    property = envVars.get(PREFIX + propertyKey.toUpperCase(Locale.ENGLISH));
    if (property != null) {
      return property;
    }
    // 3. Read .properties file with variable replacement.
    property = properties.getProperty(propertyKey, defaultValue);
    return property == null ? null : parser.parse(property);
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

  public String getHookBeforeNew() {
    return hookBeforeNew;
  }

  public String getHookAfterNew() {
    return hookAfterNew;
  }

  public Properties getVariables() {
    return variables;
  }
}
