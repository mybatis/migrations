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

import java.nio.charset.Charset;
import java.util.Properties;

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

  private final Properties variables;

  protected Environment(Builder builder) {
    this.timeZone = builder.timeZone;
    this.delimiter = builder.delimiter;
    this.scriptCharset = builder.scriptCharset;
    this.fullLineDelimiter = builder.fullLineDelimiter;
    this.sendFullScript = builder.sendFullScript;
    this.autoCommit = builder.autoCommit;
    this.removeCrs = builder.removeCrs;
    this.ignoreWarnings = builder.ignoreWarnings;
    this.driverPath = builder.driverPath;
    this.driver = builder.driver;
    this.url = builder.url;
    this.username = builder.username;
    this.password = builder.password;
    this.hookBeforeUp = builder.hookBeforeUp;
    this.hookBeforeEachUp = builder.hookBeforeEachUp;
    this.hookAfterEachUp = builder.hookAfterEachUp;
    this.hookAfterUp = builder.hookAfterUp;
    this.hookBeforeDown = builder.hookBeforeDown;
    this.hookBeforeEachDown = builder.hookBeforeEachDown;
    this.hookAfterEachDown = builder.hookAfterEachDown;
    this.hookAfterDown = builder.hookAfterDown;
    this.hookBeforeNew = builder.hookBeforeNew;
    this.hookAfterNew = builder.hookAfterNew;
    this.variables = builder.variables;
  }

  public static class Builder {
    protected String timeZone = "GMT+0:00";
    protected String delimiter = ";";
    protected String scriptCharset = Charset.defaultCharset().name();
    protected boolean fullLineDelimiter;
    protected boolean sendFullScript;
    protected boolean autoCommit;
    protected boolean removeCrs;
    protected boolean ignoreWarnings = true;
    protected String driverPath;
    protected String driver;
    protected String url;
    protected String username;
    protected String password;

    protected String hookBeforeUp;
    protected String hookBeforeEachUp;
    protected String hookAfterEachUp;
    protected String hookAfterUp;
    protected String hookBeforeDown;
    protected String hookBeforeEachDown;
    protected String hookAfterEachDown;
    protected String hookAfterDown;

    protected String hookBeforeNew;
    protected String hookAfterNew;

    protected Properties variables = new Properties();

    public Builder() {
      super();
    }

    public Builder timeZone(String timeZone) {
      if (timeZone != null) {
        this.timeZone = timeZone;
      }
      return this;
    }

    public Builder delimiter(String delimiter) {
      if (delimiter != null)
        this.delimiter = delimiter;
      return this;
    }

    public Builder scriptCharset(String scriptCharset) {
      if (scriptCharset != null) {
        this.scriptCharset = scriptCharset;
      }
      return this;
    }

    public Builder fullLineDelimiter(boolean fullLineDelimiter) {
      this.fullLineDelimiter = fullLineDelimiter;
      return this;
    }

    public Builder sendFullScript(boolean sendFullScript) {
      this.sendFullScript = sendFullScript;
      return this;
    }

    public Builder autoCommit(boolean autoCommit) {
      this.autoCommit = autoCommit;
      return this;
    }

    public Builder removeCrs(boolean removeCrs) {
      this.removeCrs = removeCrs;
      return this;
    }

    public Builder ignoreWarnings(boolean ignoreWarnings) {
      this.ignoreWarnings = ignoreWarnings;
      return this;
    }

    public Builder driverPath(String driverPath) {
      this.driverPath = driverPath;
      return this;
    }

    public Builder driver(String driver) {
      this.driver = driver;
      return this;
    }

    public Builder url(String url) {
      this.url = url;
      return this;
    }

    public Builder username(String username) {
      this.username = username;
      return this;
    }

    public Builder password(String password) {
      this.password = password;
      return this;
    }

    public Builder hookBeforeUp(String hookBeforeUp) {
      this.hookBeforeUp = hookBeforeUp;
      return this;
    }

    public Builder hookBeforeEachUp(String hookBeforeEachUp) {
      this.hookBeforeEachUp = hookBeforeEachUp;
      return this;
    }

    public Builder hookAfterEachUp(String hookAfterEachUp) {
      this.hookAfterEachUp = hookAfterEachUp;
      return this;
    }

    public Builder hookAfterUp(String hookAfterUp) {
      this.hookAfterUp = hookAfterUp;
      return this;
    }

    public Builder hookBeforeDown(String hookBeforeDown) {
      this.hookBeforeDown = hookBeforeDown;
      return this;
    }

    public Builder hookBeforeEachDown(String hookBeforeEachDown) {
      this.hookBeforeEachDown = hookBeforeEachDown;
      return this;
    }

    public Builder hookAfterEachDown(String hookAfterEachDown) {
      this.hookAfterEachDown = hookAfterEachDown;
      return this;
    }

    public Builder hookAfterDown(String hookAfterDown) {
      this.hookAfterDown = hookAfterDown;
      return this;
    }

    public Builder hookBeforeNew(String hookBeforeNew) {
      this.hookBeforeNew = hookBeforeNew;
      return this;
    }

    public Builder hookAfterNew(String hookAfterNew) {
      this.hookAfterNew = hookAfterNew;
      return this;
    }

    public Builder variables(Properties variables) {
      if (variables != null) {
        this.variables = variables;
      }
      return this;
    }

    public Environment build() {
      return new Environment(this);
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
    return variables;
  }
}
