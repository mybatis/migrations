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

import org.apache.ibatis.migration.MigrationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Environment {
  public static final String CHANGELOG = "changelog";

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

  private final Properties userVariables;

  public Environment(File file) {
    try (FileInputStream inputStream = new FileInputStream(file)) {
      final Properties prop = new Properties();
      prop.load(inputStream);

      resolveProperties(prop);

      this.timeZone = prop.getProperty(ENVIRONMENT_KEY.time_zone.name(), "GMT+0:00");
      this.delimiter = prop.getProperty(ENVIRONMENT_KEY.delimiter.name(), ";");
      this.scriptCharset = prop.getProperty(ENVIRONMENT_KEY.script_char_set.name(), Charset.defaultCharset().name());
      this.fullLineDelimiter = Boolean.valueOf(prop.getProperty(ENVIRONMENT_KEY.full_line_delimiter.name()));
      this.sendFullScript = Boolean.valueOf(prop.getProperty(ENVIRONMENT_KEY.send_full_script.name()));
      this.autoCommit = Boolean.valueOf(prop.getProperty(ENVIRONMENT_KEY.auto_commit.name()));
      this.removeCrs = Boolean.valueOf(prop.getProperty(ENVIRONMENT_KEY.remove_crs.name()));
      this.ignoreWarnings = Boolean.valueOf(prop.getProperty(ENVIRONMENT_KEY.ignore_warnings.name(), "true"));

      this.driverPath = prop.getProperty(ENVIRONMENT_KEY.driver_path.name());
      this.driver = prop.getProperty(ENVIRONMENT_KEY.driver.name());
      this.url = prop.getProperty(ENVIRONMENT_KEY.url.name());
      this.username = prop.getProperty(ENVIRONMENT_KEY.username.name());
      this.password = prop.getProperty(ENVIRONMENT_KEY.password.name());

      this.hookBeforeUp = prop.getProperty(ENVIRONMENT_KEY.hook_before_up.name());
      this.hookBeforeEachUp = prop.getProperty(ENVIRONMENT_KEY.hook_before_each_up.name());
      this.hookAfterEachUp = prop.getProperty(ENVIRONMENT_KEY.hook_after_each_up.name());
      this.hookAfterUp = prop.getProperty(ENVIRONMENT_KEY.hook_after_up.name());
      this.hookBeforeDown = prop.getProperty(ENVIRONMENT_KEY.hook_before_down.name());
      this.hookBeforeEachDown = prop.getProperty(ENVIRONMENT_KEY.hook_before_each_down.name());
      this.hookAfterEachDown = prop.getProperty(ENVIRONMENT_KEY.hook_after_each_down.name());
      this.hookAfterDown = prop.getProperty(ENVIRONMENT_KEY.hook_after_down.name());

      this.hookBeforeNew = prop.getProperty(ENVIRONMENT_KEY.hook_before_new.name());
      this.hookAfterNew = prop.getProperty(ENVIRONMENT_KEY.hook_after_new.name());

      this.userVariables = new Properties();

      final Set<String> envKeys = Stream.of(ENVIRONMENT_KEY.values()).map(Enum::name).collect(Collectors.toSet());
      userVariables.putAll(prop.keySet().stream().map(Object::toString).filter(k -> !envKeys.contains(k))
          .collect(Collectors.toMap(Function.identity(), prop::getProperty)));
    } catch (FileNotFoundException e) {
      throw new MigrationException("Environment file missing: " + file.getAbsolutePath());
    } catch (IOException e) {
      throw new MigrationException("Error loading environment properties.  Cause: " + e, e);
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

  public String getHookBeforeNew() {
    return hookBeforeNew;
  }

  public String getHookAfterNew() {
    return hookAfterNew;
  }

  public Properties getVariables() {
    return userVariables;
  }

  private static void resolveProperties(final Properties properties) {
    final Map<String, String> env = System.getenv();
    final Properties sysProps = System.getProperties();
    final Map<String, String> systemProperties = Stream.of(ENVIRONMENT_KEY.values()).map(Enum::name)
        .filter(sysProps::containsKey).collect(Collectors.toMap(Object::toString, sysProps::getProperty));

    properties.putAll(systemProperties);

    final Map<String, String> environmentProperties = Stream.of(ENVIRONMENT_KEY.values())
        .map(k -> k.name().toUpperCase()).filter(env::containsKey)
        .collect(Collectors.toMap(String::toLowerCase, env::get));

    properties.putAll(environmentProperties);
  }
}
