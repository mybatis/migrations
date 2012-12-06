package org.apache.ibatis.migration;

import org.apache.ibatis.migration.commands.Command;
import org.apache.ibatis.migration.options.SelectedOptions;

import java.io.File;
import java.io.PrintStream;
import java.util.Date;

import static org.apache.ibatis.migration.commands.Commands.resolveCommand;
import static org.apache.ibatis.migration.options.OptionsParser.parse;

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
      console.printf("\nERROR: %s", e.getMessage());
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
      console.printf("-- MyBatis Migrations %s%n", (exceptionCaught) ? "FAILURE" : "SUCCESS");
      console.printf("-- Total time: %ss%n", ((System.currentTimeMillis() - start) / 1000));
      console.printf("-- Finished at: %s%n", new Date());
      printMemoryUsage();
      console.printf("------------------------------------------------------------------------%n");
    }
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
    console.printf("%n");
    console.printf("Commands:%n");
    console.printf("  info               Display build version informations.%n");
    console.printf("  init               Creates (if necessary) and initializes a migration path.%n");
    console.printf("  bootstrap          Runs the bootstrap SQL script (see scripts/bootstrap.sql for more).%n");
    console.printf("  new <description>  Creates a new migration with the provided description.%n");
    console.printf("  up [n]             Run unapplied migrations, ALL by default, or 'n' specified.%n");
    console.printf(
        "  down [n]           Undoes migrations applied to the database. ONE by default or 'n' specified.%n");
    console.printf("  version <version>  Migrates the database up or down to the specified version.%n");
    console.printf("  pending            Force executes pending migrations out of order (not recommended).%n");
    console.printf("  status             Prints the changelog from the database if the changelog table exists.%n");
    console.printf(
        "  script <v1> <v2>   Generates a delta migration script from version v1 to v2 (undo if v1 > v2).%n");
    console.printf("%n");
    console.printf("  * Shortcuts are accepted by using the first few (unambiguous) letters of each command..%n");
    console.printf("%n");
  }
}
