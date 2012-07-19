package org.apache.ibatis.migration;

import org.apache.ibatis.migration.commands.BootstrapCommand;
import org.apache.ibatis.migration.commands.DownCommand;
import org.apache.ibatis.migration.commands.InfoCommand;
import org.apache.ibatis.migration.commands.InitializeCommand;
import org.apache.ibatis.migration.commands.NewCommand;
import org.apache.ibatis.migration.commands.PendingCommand;
import org.apache.ibatis.migration.commands.ScriptCommand;
import org.apache.ibatis.migration.commands.StatusCommand;
import org.apache.ibatis.migration.commands.UpCommand;
import org.apache.ibatis.migration.commands.VersionCommand;
import org.apache.ibatis.migration.options.OptionsParser;
import org.apache.ibatis.migration.options.SelectedOptions;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class CommandLine {
    private final PrintStream console = System.out;
    private final String[] args;

    private static final String INFO = "info";
    private static final String INIT = "init";
    private static final String BOOTSTRAP = "bootstrap";
    private static final String NEW = "new";
    private static final String UP = "up";
    private static final String DOWN = "down";
    private static final String PENDING = "pending";
    private static final String SCRIPT = "script";
    private static final String VERSION = "version";
    private static final String STATUS = "status";
    private static final Set<String> KNOWN_COMMANDS = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(INFO, INIT, NEW, UP, VERSION, DOWN, PENDING, STATUS, BOOTSTRAP, SCRIPT)));

    public CommandLine(String[] args) {
        this.args = args;
    }

    public void execute() {
        final SelectedOptions selectedOptions = OptionsParser.parse(args);
        if (!validOptions(selectedOptions) || selectedOptions.needsHelp()) {
            printUsage();
        } else {
            try {
                runCommand(selectedOptions);
            } catch (Exception e) {
                console.printf("\nERROR: %s", e.getMessage());
                if (selectedOptions.isTrace()) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void runCommand(SelectedOptions selectedOptions) {
        final String command = selectedOptions.getCommand();


        console.printf("------------------------------------------------------------------------%n");
        console.printf("MyBatis Migrations - %s%n", command);
        console.printf("------------------------------------------------------------------------%n");

        long start = System.currentTimeMillis();
        int exit = 0;

        try {
            final String params = selectedOptions.getParams();
            final File repository = selectedOptions.getRepository();
            final String environment = selectedOptions.getEnvironment();
            final String template = selectedOptions.getTemplate();
            final boolean force = selectedOptions.isForce();

            if (INFO.equals(command)) {
                new InfoCommand(System.out).execute(params);
            } else if (INIT.equals(command)) {
                new InitializeCommand(repository, environment, force).execute(params);
            } else if (BOOTSTRAP.equals(command)) {
                new BootstrapCommand(repository, environment, force).execute(params);
            } else if (NEW.equals(command)) {
                new NewCommand(repository, environment, template, force).execute(params);
            } else if (STATUS.equals(command)) {
                new StatusCommand(repository, environment, force).execute(params);
            } else if (UP.equals(command)) {
                new UpCommand(repository, environment, force).execute(params);
            } else if (VERSION.equals(command)) {
                new VersionCommand(repository, environment, force).execute(params);
            } else if (PENDING.equals(command)) {
                new PendingCommand(repository, environment, force).execute(params);
            } else if (DOWN.equals(command)) {
                new DownCommand(repository, environment, force).execute(params);
            } else if (SCRIPT.equals(command)) {
                new ScriptCommand(repository, environment, force).execute(params);
            } else {
                String match = null;
                for (String knownCommand : KNOWN_COMMANDS) {
                    if (knownCommand.startsWith(command)) {
                        if (match != null) {
                            throw new MigrationException("Ambiguous command shortcut: " + command);
                        }
                        match = knownCommand;
                    }
                }
                if (match != null) {
                    selectedOptions.setCommand(match);
                    runCommand(selectedOptions);
                } else {
                    throw new MigrationException("Attempt to execute unknown command: " + command);
                }
            }
        } finally {
            console.printf("------------------------------------------------------------------------%n");
            console.printf("MyBatis Migrations %s%n", (exit < 0) ? "FAILURE" : "SUCCESS");
            console.printf("Total time: %ss%n", ((System.currentTimeMillis() - start) / 1000));
            console.printf("Finished at: %s%n", new Date());

            printMemoryUsage();

            console.printf("------------------------------------------------------------------------%n");
        }
    }

    private void printMemoryUsage() {
        final Runtime runtime = Runtime.getRuntime();
        final int megaUnit = 1024 * 1024;
        final long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / megaUnit;
        final long totalMemory = runtime.totalMemory() / megaUnit;

        console.printf("Final Memory: %sM/%sM%n", usedMemory, totalMemory);
    }

    private boolean validOptions(SelectedOptions selectedOptions) {
        final File repository = selectedOptions.getRepository();
        if (repository.exists() && !repository.isDirectory()) {
            console.printf("Migrations path must be a directory: %s%n", repository.getAbsolutePath());
            return false;
        } else if (!selectedOptions.needsHelp() && selectedOptions.getCommand() == null) {
            console.printf("No command specified.%n");
            return false;
        }

        return true;
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
