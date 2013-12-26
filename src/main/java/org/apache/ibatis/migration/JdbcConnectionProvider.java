package org.apache.ibatis.migration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcConnectionProvider implements ConnectionProvider {

  private String url;

  private String username;

  private String password;

  public JdbcConnectionProvider(String driver, String url, String username, String password) throws Exception {
    super();
    this.url = url;
    this.username = username;
    this.password = password;
    Class.forName(driver);
  }

  @Override
  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(url, username, password);
  }

}
