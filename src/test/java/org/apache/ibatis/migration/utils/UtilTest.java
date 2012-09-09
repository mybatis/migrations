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
