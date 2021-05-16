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

import static org.junit.Assert.*;

import java.io.File;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.migration.Migrator;
import org.apache.ibatis.migration.utils.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class SystemPropertyTest {

  @Rule
  public final ExpectedSystemExit exit = ExpectedSystemExit.none();

  @Rule
  public final SystemOutRule out = new SystemOutRule().enableLog();

  @Rule
  public final RestoreSystemProperties restoreSysProps = new RestoreSystemProperties();

  @Rule
  public final EnvironmentVariables envVars = new EnvironmentVariables();

  private static File dir;

  @BeforeClass
  public static void init() throws Exception {
    dir = Resources.getResourceAsFile("org/apache/ibatis/migration/system_property/testdir");
  }

  @Before
  public void beforeEachTest() {
    exit.expectSystemExit();
    exit.checkAssertionAfterwards(new Assertion() {
      public void checkAssertion() {
        assertEquals("", out.getLog());
      }
    });
    out.clearLog();
  }

  @After
  public void afterEachTest() {
    out.clearLog();
    System.exit(0);
  }

  @Test
  public void testEnvironmentVariables() throws Exception {
    envVars.set("MIGRATIONS_DRIVER", "org.hsqldb.jdbcDriver");
    envVars.set("username", "Pocahontas");
    envVars.set("var1", "Variable 1");
    envVars.set("MIGRATIONS_VAR3", "Variable 3");
    envVars.set("migrations_var4", "Variable 4");
    envVars.set("MIGRATIONS_VAR5", "Variable 5");

    assertEnvironment();
  }

  @Test
  public void testSystemProperties() throws Exception {
    System.setProperty("MIGRATIONS_DRIVER", "org.hsqldb.jdbcDriver");
    System.setProperty("username", "Pocahontas");
    System.setProperty("var1", "Variable 1");
    System.setProperty("MIGRATIONS_VAR3", "Variable 3");
    System.setProperty("migrations_var4", "Variable 4");
    System.setProperty("MIGRATIONS_VAR5", "Variable 5");
    // Set duplicate env vars to assert priority
    envVars.set("MIGRATIONS_DRIVER", "bogus_driver");
    envVars.set("MIGRATIONS_VAR3", "bogus_var3");

    assertEnvironment();
  }

  private void assertEnvironment() {
    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "up", "1", "--trace"));

    String output = out.getLog();
    assertTrue(output.contains("SUCCESS"));
    assertTrue(output.contains("username: Pocahontas"));
    assertTrue(output.contains("var1: Variable 1"));
    assertTrue(output.contains("var2: ${var2}"));
    assertTrue(output.contains("var3: Variable 3"));
    assertTrue(output.contains("var4: Variable 4"));
    assertTrue(output.contains("var5: Variable 5"));
    assertTrue(output.contains("Var5: Var5 in properties file"));

    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "down", "1"));
  }
}
