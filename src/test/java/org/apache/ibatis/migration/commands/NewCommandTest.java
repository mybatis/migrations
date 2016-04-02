package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.options.SelectedOptions;
import org.apache.ibatis.migration.options.SelectedPaths;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NewCommandTest {
  private SelectedOptions newSelectedOption;
  private SelectedPaths selectedPaths;

  @Before
  public void setup() {
    URL url = getClass().getClassLoader().getResource("org/apache/ibatis/migration/commands");

    selectedPaths = new SelectedPaths();
    selectedPaths.setBasePath(new File(url.getFile()));

    newSelectedOption = new SelectedOptions();
    newSelectedOption.setCommand("New");
    newSelectedOption.setPaths(selectedPaths);
  }

  @Test
  (expected = MigrationException.class)
  public void testThrowsExceptionWhenDescriptionIsNotSupplied() {
    NewCommand newCommand = new NewCommand(newSelectedOption);
    newCommand.execute("");
  }

  @Test
  public void testSpacesInDescriptionIsReplacedWithUnderscores() {
    NewCommand newCommand = new NewCommand(newSelectedOption);
    newCommand.execute("must be underscores instead of spaces between words");

    File[] files = selectedPaths.getScriptPath().listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.contains("must_be_underscores_instead_of_spaces_between_words.sql");
        }
    });

    assertEquals(1, files.length);
    files[0].delete();
  }

    @Test
    public void testNewFileStartsWithNumberSeqence() {
      newSelectedOption.setEnvironment("development_useSeqNum");

      NewCommand newCommand = new NewCommand(newSelectedOption);
      newCommand.execute("should start with one");

      File[] files = selectedPaths.getScriptPath().listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.indexOf("1")==0;
        }
      });

      assertEquals(1, files.length);
      files[0].delete();
    }

    @Test
    public void testSequenceNumberIncrementedOnEachNewCommand() {
        newSelectedOption.setEnvironment("development_useSeqNum");

        NewCommand newCommand = new NewCommand(newSelectedOption);

        newCommand.execute("one");
        newCommand.execute("two");
        newCommand.execute("three");

        assertEquals("4", newCommand.getNextIDAsString());

        // cleaning up after the test
        for (File file : selectedPaths.getScriptPath().listFiles()) {
            if (!file.getName().equals(".gitignore")) {
              file.delete();
            }
        }
    }

    @Test
    public void testScriptsDirWithOnlyBootstrapFileWhichHasNoSequenceNumber() throws IOException {
      File bootStrapFile = new File(selectedPaths.getScriptPath() + "/bootstrap.sql");
      bootStrapFile.createNewFile();
      assertTrue(bootStrapFile.exists());

      newSelectedOption.setEnvironment("development_useSeqNum");
      NewCommand newCommand = new NewCommand(newSelectedOption);

      assertEquals("1", newCommand.getNextIDAsString());

      bootStrapFile.delete();
    }
}