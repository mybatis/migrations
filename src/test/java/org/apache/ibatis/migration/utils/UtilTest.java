/*
 *    Copyright 2010-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.migration.utils;

import static org.apache.ibatis.migration.utils.Util.file;
import static org.apache.ibatis.migration.utils.Util.isOption;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

class UtilTest {
  @Test
  void testIsOption() {
    assertTrue(isOption("--properOption"), "Util doesn't recognize proper option");
    assertFalse(isOption("-improperOption"), "Util doesn't recognize improper option");
    assertTrue(isOption("--properOptionValue=value"), "Util doesn't recognize proper option with value");
    assertFalse(isOption("--missingOptionValue="), "Util doesn't recognize proper option with value");
  }

  @Test
  void testFile() {
    final File parentDirectory = new File(".");
    final String childFile = "child.file";
    final File absoluteFile = file(parentDirectory, childFile);

    assertThat(absoluteFile.getAbsolutePath()).startsWith(parentDirectory.getAbsolutePath());
    assertThat(absoluteFile.getAbsolutePath()).endsWith(File.separator + childFile);
  }
}
