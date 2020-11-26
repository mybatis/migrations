/**
 *    Copyright 2010-2020 the original author or authors.
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcConnectionProvider implements ConnectionProvider {
  private final String url;
  private final String username;
  private final String password;

  public JdbcConnectionProvider(String driver, String url, String username, String password) throws Exception {
    this(null, driver, url, username, password);
  }

  public JdbcConnectionProvider(ClassLoader classLoader, String driver, String url, String username, String password)
      throws Exception {
    this.url = url;
    this.username = username;
    this.password = password;

    loadDriver(classLoader, driver);
  }

  @Override
  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(url, username, password);
  }

  private static void loadDriver(ClassLoader classLoader, String driver) throws ClassNotFoundException {
    if (classLoader != null) {
      Class.forName(driver, true, classLoader);
    } else {
      Class.forName(driver);
    }
  }
}
