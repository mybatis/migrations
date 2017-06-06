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
package org.apache.ibatis.migration.hook;

import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;

public class HookContext {
  private ConnectionProvider connectionProvider;
  private ScriptRunner scriptRunner;
  private Change change;

  public HookContext(ConnectionProvider connectionProvider, ScriptRunner scriptRunner, Change change) {
    super();
    this.connectionProvider = connectionProvider;
    this.scriptRunner = scriptRunner;
    this.change = change;
  }

  /**
   * @return A new {@link Connection} to the database. The returned connection must be closed.
   * @throws SQLException
   *           If a database access error occurs.
   */
  public Connection getConnection() throws SQLException {
    return connectionProvider.getConnection();
  }

  /**
   * @param reader
   *          Source of the SQL to execute.
   */
  public void executeSql(Reader reader) {
    scriptRunner.runScript(reader);
  }

  /**
   * @param sql
   *          SQL to execute.
   */
  public void executeSql(String sql) {
    executeSql(new StringReader(sql));
  }

  /**
   * @return Returns an instance of {@link Change} object for an each hook; <code>null</code>
   *         otherwise.
   */
  public Change getChange() {
    return change;
  }
}
