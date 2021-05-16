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
package org.apache.ibatis.migration;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.migration.driver.DriverShim;

public class JdbcConnectionProvider implements ConnectionProvider {
  private static final Map<String, Driver> registeredDrivers = registeredDrivers();

  private final String url;
  private final String username;
  private final String password;

  public JdbcConnectionProvider(String driver, String url, String username, String password) {
    this(null, driver, url, username, password);
  }

  public JdbcConnectionProvider(ClassLoader classLoader, String driver, String url, String username, String password) {
    this.url = url;
    this.username = username;
    this.password = password;
    registerDriver(classLoader, driver);
  }

  @Override
  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(url, username, password);
  }

  private void registerDriver(ClassLoader classLoader, String driver) {
    registeredDrivers.computeIfAbsent(driver, (d) -> createDriverClass(classLoader, d));
  }

  private Driver createDriverClass(ClassLoader classLoader, String driver) {
    try {
      final Class<?> driverClass = classLoader == null ? Class.forName(driver)
          : Class.forName(driver, true, classLoader);
      final Driver driverInstance = (Driver) driverClass.getDeclaredConstructor().newInstance();
      final DriverShim driverShim = new DriverShim(driverInstance);
      DriverManager.registerDriver(driverShim);
      return driverShim;
    } catch (final Exception e) {
      throw new IllegalStateException("Failed to register driver " + driver, e);
    }
  }

  private static Map<String, Driver> registeredDrivers() {
    final Map<String, Driver> registeredDrivers = new HashMap<>();
    final Enumeration<Driver> drivers = DriverManager.getDrivers();
    while (drivers.hasMoreElements()) {
      final Driver driver = drivers.nextElement();
      registeredDrivers.put(driver.getClass().getName(), driver);
    }
    return registeredDrivers;
  }
}
