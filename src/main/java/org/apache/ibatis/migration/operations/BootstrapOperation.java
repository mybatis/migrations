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
package org.apache.ibatis.migration.operations;

import java.io.PrintStream;
import java.io.Reader;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.options.DatabaseOperationOption;
import org.apache.ibatis.migration.utils.Util;

public final class BootstrapOperation extends DatabaseOperation {
  private final boolean force;

  public BootstrapOperation() {
    this(false);
  }

  public BootstrapOperation(boolean force) {
    super();
    this.force = force;
  }

  public BootstrapOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader,
      DatabaseOperationOption option, PrintStream printStream) {
    try {
      if (option == null) {
        option = new DatabaseOperationOption();
      }
      if (changelogExists(connectionProvider, option) && !force) {
        println(printStream,
            "For your safety, the bootstrap SQL script will only run before migrations are applied (i.e. before the changelog exists).  If you're certain, you can run it using the --force option.");
      } else {
        Reader bootstrapReader = migrationsLoader.getBootstrapReader();
        if (bootstrapReader != null) {
          println(printStream, Util.horizontalLine("Applying: bootstrap.sql", 80));
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
