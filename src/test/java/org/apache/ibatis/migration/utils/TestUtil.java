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
package org.apache.ibatis.migration.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class TestUtil {
  public static Connection getConnection(Properties envProperties) throws SQLException, ClassNotFoundException {
    Class.forName(envProperties.getProperty("driver"));
    return DriverManager.getConnection(envProperties.getProperty("url"), envProperties.getProperty("username"),
        envProperties.getProperty("password"));
  }

  public static String[] args(String... args) {
    return args;
  }

  public static int countStr(String output, String str) {
    int count = 0;
    for (int i = 0; i < output.length();) {
      int idx = output.indexOf(str, i);
      if (idx == -1) {
        break;
      } else {
        i = idx + 1;
        count++;
      }
    }
    return count;
  }
}
