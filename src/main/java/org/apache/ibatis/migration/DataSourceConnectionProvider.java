package org.apache.ibatis.migration;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

public class DataSourceConnectionProvider implements ConnectionProvider {

  private DataSource dataSource;

  public DataSourceConnectionProvider(DataSource dataSource) {
    super();
    this.dataSource = dataSource;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

}
