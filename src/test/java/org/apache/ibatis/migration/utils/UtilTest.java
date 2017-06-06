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

import org.junit.Test;

import java.io.File;

import static org.apache.ibatis.migration.utils.Util.file;
import static org.apache.ibatis.migration.utils.Util.isOption;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.*;

public class UtilTest {
  @Test
  public void testIsOption() {
    assertTrue("Util doesn't recognize proper option", isOption("--properOption"));
    assertFalse("Util doesn't recognize improper option", isOption("-improperOption"));
    assertTrue("Util doesn't recognize proper option with value", isOption("--properOptionValue=value"));
    assertFalse("Util doesn't recognize proper option with value", isOption("--missingOptionValue="));
  }

  @Test
  public void testFile() {
    final File parentDirectory = new File(".");
    final String childFile = "child.file";
    final File absoluteFile = file(parentDirectory, childFile);

    assertThat(absoluteFile.getAbsolutePath(), startsWith(parentDirectory.getAbsolutePath()));
    assertThat(absoluteFile.getAbsolutePath(), endsWith(File.separator + childFile));
  }
}
