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
package org.apache.ibatis.migration.operations;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.options.DatabaseOperationOption;

public abstract class DatabaseOperation {

  protected void insertChangelog(Change change, Connection con, DatabaseOperationOption option) {
    try {
      ChangelogOperation operation = new ChangelogOperation(con, option);
      change.setAppliedTimestamp(generateAppliedTimeStampAsString());
      operation.insert(change);
    } catch (SQLException e) {
      throw new MigrationException("Error querying last applied migration.  Cause: " + e, e);
    }
  }

  protected List<Change> getChangelog(Connection con, DatabaseOperationOption option) {
    try {
      ChangelogOperation operation = new ChangelogOperation(con, option);
      return operation.selectAll();
    } catch (SQLException e) {
      throw new MigrationException("Error querying last applied migration.  Cause: " + e, e);
    }
  }

  protected boolean changelogExists(Connection con, DatabaseOperationOption option) {
    ChangelogOperation operation = new ChangelogOperation(con, option);
    return operation.tableExists();
  }

  protected String checkSkippedOrMissing(List<Change> changesInDb, List<Change> migrations) {
    StringBuilder warnings = new StringBuilder();
    String separator = System.lineSeparator();
    int adjust = 0;
    for (int i = 0; i < changesInDb.size(); i++) {
      Change changeInDb = changesInDb.get(i);
      int migrationIndex = migrations.indexOf(changeInDb);
      if (migrationIndex == -1) {
        // no corresponding migration script.
        warnings.append("WARNING: Missing migration script. id='").append(changeInDb.getId()).append("', description='")
            .append(changeInDb.getDescription()).append("'.").append(separator);
        adjust++;
      } else if (migrationIndex != i - adjust) {
        // Unapplied migration script(s).
        for (int j = i - adjust; j < migrationIndex; j++) {
          adjust--;
          warnings.append("WARNING: Migration script '").append(migrations.get(j).getFilename())
              .append("' was not applied to the database.").append(separator);
        }
      }
    }
    return warnings.toString();
  }

  protected ScriptRunner getScriptRunner(Connection connection, DatabaseOperationOption option,
      PrintStream printStream) {
    try {
      PrintWriter outWriter = printStream == null ? null : new PrintWriter(printStream);
      ScriptRunner scriptRunner = new ScriptRunner(connection);
      scriptRunner.setLogWriter(outWriter);
      scriptRunner.setErrorLogWriter(outWriter);
      scriptRunner.setStopOnError(option.isStopOnError());
      scriptRunner.setThrowWarning(option.isThrowWarning());
      scriptRunner.setEscapeProcessing(false);
      scriptRunner.setAutoCommit(option.isAutoCommit());
      scriptRunner.setDelimiter(option.getDelimiter());
      scriptRunner.setFullLineDelimiter(option.isFullLineDelimiter());
      scriptRunner.setSendFullScript(option.isSendFullScript());
      scriptRunner.setRemoveCRs(option.isRemoveCRs());
      return scriptRunner;
    } catch (Exception e) {
      throw new MigrationException("Error creating ScriptRunner.  Cause: " + e, e);
    }
  }

  public static String generateAppliedTimeStampAsString() {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.sql.Date(System.currentTimeMillis()));
  }

  protected void println(PrintStream printStream) {
    if (printStream != null) {
      printStream.println();
    }
  }

  protected void println(PrintStream printStream, String text) {
    if (printStream != null) {
      printStream.println(text);
    }
  }
}
