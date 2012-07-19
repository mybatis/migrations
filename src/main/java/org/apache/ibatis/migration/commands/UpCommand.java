package org.apache.ibatis.migration.commands;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationReader;
import org.apache.ibatis.migration.options.SelectedOptions;
import org.apache.ibatis.migration.utils.Util;

import java.io.File;
import java.util.List;

public class UpCommand extends BaseCommand {
    private final boolean runOneStepOnly;

    public UpCommand(SelectedOptions options) {
        this(options, false);
    }

    public UpCommand(SelectedOptions options, boolean runOneStepOnly) {
        super(options);
        this.runOneStepOnly = runOneStepOnly;
    }

    public void execute(String... params) {
        try {
            Change lastChange = null;
            if (changelogExists()) {
                lastChange = getLastAppliedChange();
            }
            List<Change> migrations = getMigrations();
            int steps = 0;
            for (Change change : migrations) {
                if (lastChange == null || change.getId().compareTo(lastChange.getId()) > 0) {
                    printStream.println(horizontalLine("Applying: " + change.getFilename(), 80));
                    ScriptRunner runner = getScriptRunner();
                    try {
                        final File scriptFile = Util.file(paths.getScriptPath(), change.getFilename());
                        runner.runScript(new MigrationReader(scriptFileReader(scriptFile),
                            false,
                            environmentProperties()));
                    } finally {
                        runner.closeConnection();
                    }
                    insertChangelog(change);
                    printStream.println();
                    steps++;
                    final int limit = getStepCountParameter(Integer.MAX_VALUE, params);
                    if (steps == limit || runOneStepOnly) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new MigrationException("Error executing command.  Cause: " + e, e);
        }
    }

}
