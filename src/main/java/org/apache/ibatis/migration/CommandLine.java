/**
 *    Copyright 2010-2018 the original author or authors.
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
package org.apache.ibatis.migration;

import static org.apache.ibatis.migration.commands.Commands.resolveCommand;
import static org.apache.ibatis.migration.options.OptionsParser.parse;

import java.io.File;
import java.io.PrintStream;
import java.util.Date;

import org.apache.ibatis.migration.commands.Command;
import org.apache.ibatis.migration.options.Options;
import org.apache.ibatis.migration.options.SelectedOptions;
import org.apache.ibatis.migration.utils.Util;
import org.apache.ibatis.migration.ConsoleColors;

public class CommandLine {
  private final PrintStream console = System.out;
  private final String[] args;

  public CommandLine(String[] args) {
    this.args = args;
  }

  public void execute() {
    final SelectedOptions selectedOptions = parse(args);
    try {
      if (!validOptions(selectedOptions) || selectedOptions.needsHelp()) {
        printUsage();
      } else {
        runCommand(selectedOptions);
      }
    } catch (Exception e) {
      String errorMessage = e.getMessage();

      if (hasColor(selectedOptions)) {
        console.printf(ConsoleColors.RED + "\nERROR: %s%n", errorMessage + ConsoleColors.RESET);
      } else {
        console.printf("\nERROR: %s%n", errorMessage);
      }

      if (selectedOptions.isTrace()) {
        e.printStackTrace();
      }
      System.exit(1); // Issue 730
    }
  }

  private void runCommand(SelectedOptions selectedOptions) {
    final String commandString = selectedOptions.getCommand();

    console.printf("------------------------------------------------------------------------%n");
    console.printf("-- MyBatis Migrations - %s%n", commandString);
    console.printf("------------------------------------------------------------------------%n");

    long start = System.currentTimeMillis();
    boolean exceptionCaught = false;

    try {
      final Command command = resolveCommand(commandString.toUpperCase(), selectedOptions);
      command.execute(selectedOptions.getParams());
    } catch (Throwable t) {
      exceptionCaught = true;
      if (t instanceof MigrationException) {
        throw (MigrationException) t;
      } else {
        throw new MigrationException(t);
      }
    } finally {
      console.printf("------------------------------------------------------------------------%n");

      if (hasColor(selectedOptions)) {
        console.printf("-- MyBatis Migrations %s%s%s%n", (exceptionCaught) ? ConsoleColors.RED : ConsoleColors.GREEN,
            (exceptionCaught) ? "FAILURE" : "SUCCESS", ConsoleColors.RESET);
      } else {
        console.printf("-- MyBatis Migrations %s%n", (exceptionCaught) ? "FAILURE" : "SUCCESS");
      }

      console.printf("-- Total time: %ss%n", ((System.currentTimeMillis() - start) / 1000));
      console.printf("-- Finished at: %s%n", new Date());
      printMemoryUsage();
      console.printf("------------------------------------------------------------------------%n");
    }
  }

  protected boolean hasColor(SelectedOptions selectedOptions) {
    return selectedOptions.hasColor() || Util.getPropertyOptionAsBoolean(Options.COLOR.toString().toLowerCase());
  }

  private void printMemoryUsage() {
    final Runtime runtime = Runtime.getRuntime();
    final int megaUnit = 1024 * 1024;
    final long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / megaUnit;
    final long totalMemory = runtime.totalMemory() / megaUnit;

    console.printf("-- Final Memory: %sM/%sM%n", usedMemory, totalMemory);
  }

  private boolean validOptions(SelectedOptions selectedOptions) {
    if (!selectedOptions.needsHelp() && selectedOptions.getCommand() == null) {
      console.printf("No command specified.%n");
      return false;
    }

    return validBasePath(selectedOptions.getPaths().getBasePath());
  }

  private boolean validBasePath(File basePath) {
    final boolean validDirectory = basePath.exists() && basePath.isDirectory();

    if (!validDirectory) {
      console.printf("Migrations path must be a directory: %s%n", basePath.getAbsolutePath());
    }

    return validDirectory;
  }

  private void printUsage() {
    console.printf(
        "%nUsage: migrate command [parameter] [--path=<directory>] [--env=<environment>] [--template=<path to custom template>]%n%n");
    console.printf("--path=<directory>   Path to repository.  Default current working directory.%n");
    console.printf("--env=<environment>  Environment to configure. Default environment is 'development'.%n");
    console.printf("--template=<template>  Path to custom template for creating new sql scripts.%n");
    console.printf("--force              Forces script to continue even if SQL errors are encountered.%n");
    console.printf("--help               Displays this usage message.%n");
    console.printf("--trace              Shows additional error details (if any).%n");
    console.printf("--quiet              Suppresses output.%n");
    console.printf("--color              Colorize output.%n");
    console.printf("%n");
    console.printf("Commands:%n");
    console.printf("  info               Display build version informations.%n");
    console.printf("  init               Creates (if necessary) and initializes a migration path.%n");
    console.printf("  bootstrap          Runs the bootstrap SQL script (see scripts/bootstrap.sql for more).%n");
    console.printf("  new <description>  Creates a new migration with the provided description.%n");
    console.printf("  up [n]             Run unapplied migrations, ALL by default, or 'n' specified.%n");
    console
        .printf("  down [n]           Undoes migrations applied to the database. ONE by default or 'n' specified.%n");
    console.printf("  version <version>  Migrates the database up or down to the specified version.%n");
    console.printf("  pending            Force executes pending migrations out of order (not recommended).%n");
    console.printf("  status             Prints the changelog from the database if the changelog table exists.%n");
    console
        .printf("  script <v1> <v2>   Generates a delta migration script from version v1 to v2 (undo if v1 > v2).%n");
    console.printf("%n");
    console.printf("  * Shortcuts are accepted by using the first few (unambiguous) letters of each command..%n");
    console.printf("%n");
  }
}
