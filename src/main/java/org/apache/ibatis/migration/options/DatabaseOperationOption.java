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
package org.apache.ibatis.migration.options;

public class DatabaseOperationOption {
  private static final String DEFAULT_CHANGELOG_TABLE = "CHANGELOG";

  private static final String DEFAULT_DELIMITER = ";";

  private String changelogTable;

  private boolean stopOnError = true;

  private boolean throwWarning = true;

  private boolean autoCommit;

  private boolean sendFullScript;

  private boolean removeCRs;

  private boolean escapeProcessing = true;

  private boolean fullLineDelimiter = false;

  private String delimiter;

  public String getChangelogTable() {
    return changelogTable == null ? DEFAULT_CHANGELOG_TABLE : changelogTable;
  }

  public void setChangelogTable(String changelogTable) {
    this.changelogTable = changelogTable;
  }

  public boolean isStopOnError() {
    return stopOnError;
  }

  public void setStopOnError(boolean stopOnError) {
    this.stopOnError = stopOnError;
  }

  public boolean isThrowWarning() {
    return throwWarning;
  }

  public void setThrowWarning(boolean throwWarning) {
    this.throwWarning = throwWarning;
  }

  public boolean isAutoCommit() {
    return autoCommit;
  }

  public void setAutoCommit(boolean autoCommit) {
    this.autoCommit = autoCommit;
  }

  public boolean isSendFullScript() {
    return sendFullScript;
  }

  public void setSendFullScript(boolean sendFullScript) {
    this.sendFullScript = sendFullScript;
  }

  public boolean isRemoveCRs() {
    return removeCRs;
  }

  public void setRemoveCRs(boolean removeCRs) {
    this.removeCRs = removeCRs;
  }

  public boolean isEscapeProcessing() {
    return escapeProcessing;
  }

  public void setEscapeProcessing(boolean escapeProcessing) {
    this.escapeProcessing = escapeProcessing;
  }

  public boolean isFullLineDelimiter() {
    return fullLineDelimiter;
  }

  public void setFullLineDelimiter(boolean fullLineDelimiter) {
    this.fullLineDelimiter = fullLineDelimiter;
  }

  public String getDelimiter() {
    return delimiter == null ? DEFAULT_DELIMITER : delimiter;
  }

  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }
}
