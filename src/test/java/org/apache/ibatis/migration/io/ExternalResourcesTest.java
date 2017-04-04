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
package org.apache.ibatis.migration.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExternalResourcesTest {

  private File tempFile;

  /*
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    tempFile = File.createTempFile("migration", "properties");
    tempFile.canWrite();
  }

  @Test
  public void testGetConfiguredTemplate() {
    String templateName = "";
    try {
      FileWriter fileWriter = new FileWriter(tempFile);
      try {
        fileWriter.append("new_command.template=templates/col_new_template_migration.sql");
        fileWriter.flush();
        templateName = ExternalResources.getConfiguredTemplate(tempFile.getAbsolutePath(),
            "new_command.template");
        assertEquals("templates/col_new_template_migration.sql", templateName);
      } finally {
        fileWriter.close();
      }
    } catch (Exception e) {
      fail("Test failed with execption: " + e.getMessage());
    }
  }

  @After
  public void cleanUp() {
    tempFile.delete();
  }
}
