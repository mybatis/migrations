/**
 *    Copyright 2010-2018 the original author or authors.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

public enum Util {
  ;

  private static final String MIGRATIONS_HOME = "MIGRATIONS_HOME";

  /* TODO: remove in the next major release */
  private static final String MIGRATIONS_HOME_PROPERTY_DEPRECATED = "migrationHome";

  private static final String MIGRATIONS_HOME_PROPERTY = "migrationsHome";

  private static final String MIGRATIONS_PROPERTIES = "migration.properties";

  public static String migrationsHome() {
    String migrationsHome = System.getenv(MIGRATIONS_HOME);
    // Check if there is a system property
    if (migrationsHome == null) {
      migrationsHome = System.getProperty(MIGRATIONS_HOME_PROPERTY);
      if (migrationsHome == null) {
        migrationsHome = System.getProperty(MIGRATIONS_HOME_PROPERTY_DEPRECATED);
      }
    }
    return migrationsHome;
  }

  public static boolean getPropertyOptionAsBoolean(String key) {
    return Boolean.valueOf(getPropertyOption(key));
  }

  /**
   * @param key
   * @return The value <code>null</code> if the property file does not exist or the <code>key</code> does not exist.
   * @throws FileNotFoundException
   */
  public static String getPropertyOption(String key) {
    String migrationsHome = migrationsHome();
    if (migrationsHome == null || migrationsHome.isEmpty()) {
      return null;
    }
    try {
      String path = migrationsHome + File.separator + MIGRATIONS_PROPERTIES;
      Properties properties = new Properties();
      properties.load(new FileInputStream(path));
      return properties.getProperty(key);
    } catch (Exception e) {
      return null;
    }
  }

  public static boolean isOption(String arg) {
    return arg.startsWith("--") && !arg.trim().endsWith("=");
  }

  public static File file(File path, String fileName) {
    return new File(path.getAbsolutePath() + File.separator + fileName);
  }

  public static String horizontalLine(String caption, int length) {
    StringBuilder builder = new StringBuilder();
    builder.append("==========");
    if (caption.length() > 0) {
      caption = " " + caption + " ";
      builder.append(caption);
    }
    for (int i = 0; i < length - caption.length() - 10; i++) {
      builder.append("=");
    }
    return builder.toString();
  }
}
