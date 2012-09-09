package org.apache.ibatis.migration.commands;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationReader;
import org.apache.ibatis.migration.options.SelectedOptions;
import org.apache.ibatis.migration.utils.Util;

import java.io.File;

public class BootstrapCommand extends BaseCommand {
  public BootstrapCommand(SelectedOptions options) {
    super(options);
  }

  public void execute(String... params) {
    try {
      if (changelogExists() && !options.isForce()) {
        printStream.println(
            "For your safety, the bootstrap SQL script will only run before migrations are applied (i.e. before the changelog exists).  If you're certain, you can run it using the --force option.");
      } else {
        File bootstrap = Util.file(paths.getScriptPath(), "bootstrap.sql");
        if (bootstrap.exists()) {
          printStream.println(horizontalLine("Applying: bootstrap.sql", 80));
          ScriptRunner runner = getScriptRunner();
          try {
            runner.runScript(new MigrationReader(scriptFileReader(bootstrap),
                false,
                environmentProperties()));
          } finally {
            runner.closeConnection();
          }
          printStream.println();
        } else {
          printStream.println("Error, could not run bootstrap.sql.  The file does not exist.");
        }
      }
    } catch (Exception e) {
      throw new MigrationException("Error running bootstrapper.  Cause: " + e, e);
    }
  }

}
