/*
 *    Copyright 2010-2021 the original author or authors.
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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.options.DatabaseOperationOption;

public class ChangelogOperation {
  private final Connection con;
  private final DatabaseOperationOption option;

  public ChangelogOperation(Connection con, DatabaseOperationOption option) {
    super();
    this.con = con;
    this.option = option;
  }

  public boolean tableExists() {
    try (Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select count(1) from " + option.getChangelogTable())) {
      return rs.next();
    } catch (SQLException e) {
      return false;
    }
  }

  public List<Change> selectAll() throws SQLException {
    List<Change> changes = new ArrayList<>();
    try (
        PreparedStatement stmt = con
            .prepareStatement("select ID, APPLIED_AT, DESCRIPTION from " + option.getChangelogTable() + " order by ID");
        ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        changes.add(new Change(rs.getBigDecimal(1), rs.getString(2), rs.getString(3)));
      }
      return changes;
    }
  }

  public void insert(Change change) throws SQLException {
    try (PreparedStatement stmt = con.prepareStatement(
        "insert into " + option.getChangelogTable() + " (ID, APPLIED_AT, DESCRIPTION) values (?,?,?)")) {
      stmt.setBigDecimal(1, change.getId());
      stmt.setString(2, change.getAppliedTimestamp());
      stmt.setString(3, change.getDescription());
      stmt.execute();
      con.commit();
    }
  }

  public void deleteById(BigDecimal id) throws SQLException {
    try (PreparedStatement stmt = con.prepareStatement("delete from " + option.getChangelogTable() + " where ID = ?")) {
      stmt.setBigDecimal(1, id);
      stmt.execute();
      con.commit();
    }
  }
}
