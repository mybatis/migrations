package org.apache.ibatis.migration.operations;

import java.io.PrintStream;
import java.io.Reader;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.options.DatabaseOperationOption;

public final class BootstrapOperation extends DatabaseOperation<BootstrapOperation> {
  private final boolean force;

  public BootstrapOperation() {
    this(false);
  }

  public BootstrapOperation(boolean force) {
    super();
    this.force = force;
  }

  @Override
  public BootstrapOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader, DatabaseOperationOption option, PrintStream printStream) {
    try {
      if (option == null) {
        option = new DatabaseOperationOption();
      }
      if (changelogExists(connectionProvider, option) && !force) {
        println(printStream, "For your safety, the bootstrap SQL script will only run before migrations are applied (i.e. before the changelog exists).  If you're certain, you can run it using the --force option.");
      } else {
        Reader bootstrapReader = migrationsLoader.getBootstrapReader();
        if (bootstrapReader != null) {
          println(printStream, horizontalLine("Applying: bootstrap.sql", 80));
          ScriptRunner runner = getScriptRunner(connectionProvider, option, printStream);
          try {
            runner.runScript(bootstrapReader);
          } finally {
            runner.closeConnection();
          }
          println(printStream);
        } else {
          println(printStream, "Error, could not run bootstrap.sql.  The file does not exist.");
        }
      }
      return this;
    } catch (Exception e) {
      throw new MigrationException("Error running bootstrapper.  Cause: " + e, e);
    }
  }
}
