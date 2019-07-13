/**
 *    Copyright 2010-2019 the original author or authors.
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
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class SystemPropertyTest {

  @Rule
  public final ExpectedSystemExit exit = ExpectedSystemExit.none();

  @Rule
  public final SystemOutRule out = new SystemOutRule().enableLog();

  private static File dir;

  @BeforeClass
  public static void init() throws Exception {
    dir = Resources.getResourceAsFile("org/apache/ibatis/migration/system_property/testdir");
  }

  @Test
  public void testSystemProperties() throws Exception {
    exit.expectSystemExit();
    exit.checkAssertionAfterwards(new Assertion() {
      public void checkAssertion() {
        assertEquals("", out.getLog());
      }
    });
    out.clearLog();

    // Set system properties
    System.setProperty("MIGRATIONS_DRIVER", "org.hsqldb.jdbcDriver");

    Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "up", "1", "--trace"));

    String output = out.getLog();
    assertTrue(output.contains("SUCCESS"));

    out.clearLog();
    System.exit(0);
  }
}
