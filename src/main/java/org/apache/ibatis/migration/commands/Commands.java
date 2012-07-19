package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.options.SelectedOptions;

import java.io.File;

public enum Commands {
    INFO,
    INIT,
    BOOTSTRAP,
    NEW,
    UP,
    DOWN,
    PENDING,
    SCRIPT,
    VERSION,
    STATUS;

    public static Command resolveCommand(String commandString, SelectedOptions selectedOptions) {
        for (Commands command : values()) {
            if (command.name().startsWith(commandString)) {
                return createCommand(command, selectedOptions);
            }
        }

        throw new MigrationException("Attempt to execute unknown command: " + commandString);
    }

    private static Command createCommand(Commands aResolvedCommand, SelectedOptions selectedOptions) {
        final File repository = selectedOptions.getRepository();
        final String environment = selectedOptions.getEnvironment();
        final String template = selectedOptions.getTemplate();
        final boolean force = selectedOptions.isForce();

        switch (aResolvedCommand) {
            case INFO:
                return new InfoCommand(System.out);
            case INIT:
                return new InitializeCommand(repository, environment, force);
            case BOOTSTRAP:
                return new BootstrapCommand(repository, environment, force);
            case NEW:
                return new NewCommand(repository, environment, template, force);
            case UP:
                return new UpCommand(repository, environment, force);
            case DOWN:
                return new DownCommand(repository, environment, force);
            case PENDING:
                return new PendingCommand(repository, environment, force);
            case SCRIPT:
                return new ScriptCommand(repository, environment, force);
            case VERSION:
                return new VersionCommand(repository, environment, force);
            case STATUS:
                return new StatusCommand(repository, environment, force);
            default:
                return new Command() {
                    public void execute(String... params) {
                        System.out.println("unknown command");
                    }
                };
        }
    }
}
