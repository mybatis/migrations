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
package org.apache.ibatis.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class Environment {

  public static final String CHANGELOG = "changelog";

  private enum SETTING_KEY {
    TIME_ZONE,

    DELIMITER,

    SCRIPT_CHAR_SET,

    FULL_LINE_DELIMITER,

    SEND_FULL_SCRIPT,

    AUTO_COMMIT,

    REMOVE_CRS,

    IGNORE_WARNINGS,

    DRIVER_PATH,

    DRIVER,

    URL,

    USERNAME,

    PASSWORD,

    HOOK_BEFORE_UP,

    HOOK_BEFORE_EACH_UP,

    HOOK_AFTER_EACH_UP,

    HOOK_AFTER_UP,

    HOOK_BEFORE_DOWN,

    HOOK_BEFORE_EACH_DOWN,

    HOOK_AFTER_EACH_DOWN,

    HOOK_AFTER_DOWN,

    HOOK_BEFORE_NEW,

    HOOK_AFTER_NEW,

    HOOK_BEFORE_SCRIPT,

    HOOK_BEFORE_EACH_SCRIPT,

    HOOK_AFTER_EACH_SCRIPT,

    HOOK_AFTER_SCRIPT;

    @Override
    public String toString() {
      return this.name().toLowerCase(Locale.ENGLISH);
    }
  }

  private static final List<String> SETTING_KEYS;

  static {
    ArrayList<String> list = new ArrayList<>();
    SETTING_KEY[] keys = SETTING_KEY.values();
    for (SETTING_KEY key : keys) {
      list.add(key.toString());
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

  private final String hookBeforeScript;
  private final String hookBeforeEachScript;
  private final String hookAfterEachScript;
  private final String hookAfterScript;

  /**
   * Prefix used to lookup environment variable or system property.
   */
  private static final String PREFIX = "MIGRATIONS_";
  private final Map<String, String> envVars = System.getenv();
  private final Properties sysProps = System.getProperties();
  private final Properties variables = new Properties();

  private final VariableReplacer parser = new VariableReplacer(Arrays.asList(sysProps, envVars));

  public Environment(File file) {
    Properties prop = mergeProperties(file);

    this.timeZone = readProperty(prop, SETTING_KEY.TIME_ZONE.toString(), "GMT+0:00");
    this.delimiter = readProperty(prop, SETTING_KEY.DELIMITER.toString(), ";");
    this.scriptCharset = readProperty(prop, SETTING_KEY.SCRIPT_CHAR_SET.toString(),
        Charset.defaultCharset().toString());
    this.fullLineDelimiter = Boolean.parseBoolean(readProperty(prop, SETTING_KEY.FULL_LINE_DELIMITER.toString()));
    this.sendFullScript = Boolean.parseBoolean(readProperty(prop, SETTING_KEY.SEND_FULL_SCRIPT.toString()));
    this.autoCommit = Boolean.parseBoolean(readProperty(prop, SETTING_KEY.AUTO_COMMIT.toString()));
    this.removeCrs = Boolean.parseBoolean(readProperty(prop, SETTING_KEY.REMOVE_CRS.toString()));
    this.ignoreWarnings = Boolean.parseBoolean(readProperty(prop, SETTING_KEY.IGNORE_WARNINGS.toString(), "true"));

    this.driverPath = readProperty(prop, SETTING_KEY.DRIVER_PATH.toString());
    this.driver = readProperty(prop, SETTING_KEY.DRIVER.toString());
    this.url = readProperty(prop, SETTING_KEY.URL.toString());
    this.username = readProperty(prop, SETTING_KEY.USERNAME.toString());
    this.password = readProperty(prop, SETTING_KEY.PASSWORD.toString());

    this.hookBeforeUp = readProperty(prop, SETTING_KEY.HOOK_BEFORE_UP.toString());
    this.hookBeforeEachUp = readProperty(prop, SETTING_KEY.HOOK_BEFORE_EACH_UP.toString());
    this.hookAfterEachUp = readProperty(prop, SETTING_KEY.HOOK_AFTER_EACH_UP.toString());
    this.hookAfterUp = readProperty(prop, SETTING_KEY.HOOK_AFTER_UP.toString());
    this.hookBeforeDown = readProperty(prop, SETTING_KEY.HOOK_BEFORE_DOWN.toString());
    this.hookBeforeEachDown = readProperty(prop, SETTING_KEY.HOOK_BEFORE_EACH_DOWN.toString());
    this.hookAfterEachDown = readProperty(prop, SETTING_KEY.HOOK_AFTER_EACH_DOWN.toString());
    this.hookAfterDown = readProperty(prop, SETTING_KEY.HOOK_AFTER_DOWN.toString());

    this.hookBeforeNew = readProperty(prop, SETTING_KEY.HOOK_BEFORE_NEW.toString());
    this.hookAfterNew = readProperty(prop, SETTING_KEY.HOOK_AFTER_NEW.toString());

    this.hookBeforeScript = readProperty(prop, SETTING_KEY.HOOK_BEFORE_SCRIPT.toString());
    this.hookBeforeEachScript = readProperty(prop, SETTING_KEY.HOOK_BEFORE_EACH_SCRIPT.toString());
    this.hookAfterEachScript = readProperty(prop, SETTING_KEY.HOOK_AFTER_EACH_SCRIPT.toString());
    this.hookAfterScript = readProperty(prop, SETTING_KEY.HOOK_AFTER_SCRIPT.toString());

    // User defined variables.
    prop.entrySet().stream().filter(e -> !SETTING_KEYS.contains(e.getKey()))
        .forEach(e -> variables.put(e.getKey(), parser.replace((String) e.getValue())));
  }

  private Properties mergeProperties(File file) {
    // 1. Load from file.
    Properties prop = loadPropertiesFromFile(file);
    // 2. Read environment variables (existing entries are overwritten).
    envVars.entrySet().stream().filter(e -> isMigrationsKey(e.getKey()))
        .forEach(e -> prop.put(normalizeKey(e.getKey()), e.getValue()));
    // 3. Read system properties (existing entries are overwritten).
    sysProps.entrySet().stream().filter(e -> isMigrationsKey((String) e.getKey()))
        .forEach(e -> prop.put(normalizeKey((String) e.getKey()), e.getValue()));
    return prop;
  }

  private String normalizeKey(String key) {
    return key.substring(PREFIX.length()).toLowerCase(Locale.ENGLISH);
  }

  private boolean isMigrationsKey(String key) {
    return key.length() > PREFIX.length() && key.toUpperCase(Locale.ENGLISH).startsWith(PREFIX);
  }

  private Properties loadPropertiesFromFile(File file) {
    Properties properties = new Properties();
    try (FileInputStream inputStream = new FileInputStream(file)) {
      properties.load(inputStream);
      return properties;
    } catch (FileNotFoundException e) {
      throw new MigrationException("Environment file missing: " + file.getAbsolutePath());
    } catch (IOException e) {
      throw new MigrationException("Error loading environment properties.  Cause: " + e, e);
    }
  }

  private String readProperty(Properties properties, String propertyKey) {
    return readProperty(properties, propertyKey, null);
  }

  private String readProperty(Properties properties, String propertyKey, String defaultValue) {
    String property = properties.getProperty(propertyKey, defaultValue);
    return property == null ? null : parser.replace(property);
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

  public String getHookBeforeScript() {
    return hookBeforeScript;
  }

  public String getHookBeforeEachScript() {
    return hookBeforeEachScript;
  }

  public String getHookAfterEachScript() {
    return hookAfterEachScript;
  }

  public String getHookAfterScript() {
    return hookAfterScript;
  }

  public Properties getVariables() {
    return variables;
  }
}
