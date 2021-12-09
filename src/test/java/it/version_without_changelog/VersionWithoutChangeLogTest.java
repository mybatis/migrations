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
package it.version_without_changelog;

import static org.junit.jupiter.api.Assertions.*;

import com.github.stefanbirkner.systemlambda.SystemLambda;

import java.io.File;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.migration.Migrator;
import org.apache.ibatis.migration.utils.TestUtil;
import org.junit.jupiter.api.Test;

class VersionWithoutChangeLogTest {

  @Test
  void shouldUpToVersionEvenWithoutChangelog() throws Exception {
    // gh-160
    File dir = Resources.getResourceAsFile("it/version_without_changelog");
    String output = SystemLambda.tapSystemOut(() -> {
      Migrator.main(TestUtil.args("--path=" + dir.getAbsolutePath(), "version", "20000101000001"));
    });
    assertFalse(output.contains("FAILURE"));
    assertTrue(output.contains("20000101000000"));
    assertTrue(output.contains("20000101000001"));
    assertFalse(output.contains("20000101000002"));
  }
}
