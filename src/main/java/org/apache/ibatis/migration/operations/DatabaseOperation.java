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
package org.apache.ibatis.migration.operations;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.jdbc.SqlRunner;
import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.options.DatabaseOperationOption;

public abstract class DatabaseOperation {

  protected void insertChangelog(Change change, ConnectionProvider connectionProvider, DatabaseOperationOption option) {
    SqlRunner runner = getSqlRunner(connectionProvider);
    change.setAppliedTimestamp(generateAppliedTimeStampAsString());
    try {
      runner.insert("insert into " + option.getChangelogTable() + " (ID, APPLIED_AT, DESCRIPTION) values (?,?,?)",
          change.getId(), change.getAppliedTimestamp(), change.getDescription());
    } catch (SQLException e) {
      throw new MigrationException("Error querying last applied migration.  Cause: " + e, e);
    } finally {
      runner.closeConnection();
    }
  }

  protected Change getLastAppliedChange(ConnectionProvider connectionProvider, DatabaseOperationOption option) {
    List<Change> changelog = getChangelog(connectionProvider, option);
    return changelog.isEmpty() ? null : changelog.get(changelog.size() - 1);
  }

  protected List<Change> getChangelog(ConnectionProvider connectionProvider, DatabaseOperationOption option) {
    SqlRunner runner = getSqlRunner(connectionProvider);
    try {
      List<Map<String, Object>> changelog = runner
          .selectAll("select ID, APPLIED_AT, DESCRIPTION from " + option.getChangelogTable() + " order by ID");
      List<Change> changes = new ArrayList<Change>();
      for (Map<String, Object> change : changelog) {
        String id = change.get("ID") == null ? null : change.get("ID").toString();
        String appliedAt = change.get("APPLIED_AT") == null ? null : change.get("APPLIED_AT").toString();
        String description = change.get("DESCRIPTION") == null ? null : change.get("DESCRIPTION").toString();
        changes.add(new Change(new BigDecimal(id), appliedAt, description));
      }
      return changes;
    } catch (SQLException e) {
      throw new MigrationException("Error querying last applied migration.  Cause: " + e, e);
    } finally {
      runner.closeConnection();
    }
  }

  protected boolean changelogExists(ConnectionProvider connectionProvider, DatabaseOperationOption option) {
    SqlRunner runner = getSqlRunner(connectionProvider);
    try {
      runner.selectAll("select ID, APPLIED_AT, DESCRIPTION from " + option.getChangelogTable());
      return true;
    } catch (SQLException e) {
      return false;
    } finally {
      runner.closeConnection();
    }
  }

  protected SqlRunner getSqlRunner(ConnectionProvider connectionProvider) {
    try {
      return new SqlRunner(connectionProvider.getConnection());
    } catch (SQLException e) {
      throw new MigrationException("Could not create SqlRunner. Cause: " + e, e);
    }
  }

  protected ScriptRunner getScriptRunner(ConnectionProvider connectionProvider, DatabaseOperationOption option,
      PrintStream printStream) {
    try {
      PrintWriter outWriter = printStream == null ? null : new PrintWriter(printStream);
      ScriptRunner scriptRunner = new ScriptRunner(connectionProvider.getConnection());
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
