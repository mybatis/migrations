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
package org.apache.ibatis.migration.system_property;

import static org.junit.jupiter.api.Assertions.*;

import com.github.stefanbirkner.systemlambda.SystemLambda;

import java.io.File;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.migration.Migrator;
import org.apache.ibatis.migration.utils.TestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SystemPropertyTest {

  private static File dir;

  @BeforeAll
  public static void init() throws Exception {
    dir = Resources.getResourceAsFile("org/apache/ibatis/migration/system_property/testdir");
  }

  @Test
  void testSystemProperties() throws Exception {
    // This test requires two environment variables
    // MIGRATIONS_VAR3 = bogus_var3
    // MIGRATIONS_ENVVAR1 = Environment variable 1
    SystemLambda.restoreSystemProperties(() -> {
      System.setProperty("MIGRATIONS_DRIVER", "org.hsqldb.jdbcDriver");
      System.setProperty("username", "Pocahontas");
      System.setProperty("var1", "Variable 1");
      System.setProperty("MIGRATIONS_VAR3", "Variable 3");
      System.setProperty("migrations_var4", "Variable 4");
      System.setProperty("MIGRATIONS_VAR5", "Variable 5");
      String output = SystemLambda.tapSystemOut(() -> {
        Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "up", "1", "--trace"));
      });
      assertTrue(output.contains("SUCCESS"));
      assertTrue(output.contains("username: Pocahontas"));
      assertTrue(output.contains("var1: Variable 1"));
      assertTrue(output.contains("var2: ${var2}"));
      assertTrue(output.contains("var3: Variable 3"), "System property should overwrite env var");
      assertTrue(output.contains("var4: Variable 4"));
      assertTrue(output.contains("var5: Variable 5"));
      assertTrue(output.contains("Var5: Var5 in properties file"));
      assertTrue(output.contains("envvar1: Environment variable 1"));

      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "down", "1"));
    });
  }
}
