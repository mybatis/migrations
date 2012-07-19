package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.options.SelectedOptions;
import org.apache.ibatis.migration.utils.Util;

import java.io.File;
import java.util.Properties;

public class InitializeCommand extends BaseCommand {
    public InitializeCommand(SelectedOptions selectedOptions) {
        super(selectedOptions);
    }

    public void execute(String... params) {
        final File basePath = paths.getBasePath();
        final File scriptPath = paths.getScriptPath();

        printStream.println("Initializing: " + basePath);

        createDirectoryIfNecessary(basePath);
        ensureDirectoryIsEmpty(basePath);

        createDirectoryIfNecessary(paths.getEnvPath());
        createDirectoryIfNecessary(scriptPath);
        createDirectoryIfNecessary(paths.getDriverPath());

        copyResourceTo("org/apache/ibatis/migration/template_README", Util.file(basePath, "README"));
        copyResourceTo("org/apache/ibatis/migration/template_environment.properties", environmentFile());
        copyResourceTo("org/apache/ibatis/migration/template_bootstrap.sql", Util.file(scriptPath, "bootstrap.sql"));
        copyResourceTo("org/apache/ibatis/migration/template_changelog.sql",
            Util.file(scriptPath, getNextIDAsString() + "_create_changelog.sql"));
        copyResourceTo("org/apache/ibatis/migration/template_migration.sql",
            Util.file(scriptPath, getNextIDAsString() + "_first_migration.sql"),
            new Properties() {
                {
                    setProperty("description", "First migration.");
                }
            });
        printStream.println("Done!");
        printStream.println();
    }

    protected void ensureDirectoryIsEmpty(File path) {
        String[] list = path.list();
        if (list.length != 0) {
            for (String entry : list) {
                if (!entry.startsWith(".")) {
                    throw new MigrationException(
                        "Directory must be empty (.svn etc allowed): " + path.getAbsolutePath());
                }
            }
        }
    }

    protected void createDirectoryIfNecessary(File path) {
        if (!path.exists()) {
            printStream.println("Creating: " + path.getName());
            if (!path.mkdirs()) {
                throw new MigrationException(
                    "Could not create directory path for an unknown reason. Make sure you have access to the directory.");
            }
        }
    }


}
