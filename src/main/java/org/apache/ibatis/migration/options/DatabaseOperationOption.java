package org.apache.ibatis.migration.options;

public class DatabaseOperationOption {
  private static final String DEFAULT_CHANGELOG_TABLE = "CHANGELOG";

  private static final String DEFAULT_DELIMITER = ";";

  private String changelogTable;

  private boolean stopOnError = true;

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
