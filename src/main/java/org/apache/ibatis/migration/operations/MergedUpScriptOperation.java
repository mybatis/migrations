/**
 *    Copyright 2010-2015 the original author or authors.
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

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.options.DatabaseOperationOption;

/**
 * @author szaboma
 */
public class MergedUpScriptOperation extends DatabaseOperation<MergedUpScriptOperation> {

    @Override
    public MergedUpScriptOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader, DatabaseOperationOption option, PrintStream printStream) {
        try {
            if (option == null) {
                option = new DatabaseOperationOption();
            }

            List<Change> pendingChanges = getPendingChanges(connectionProvider, migrationsLoader, option);

            for (Change change : pendingChanges) {
                printStream.println("-- " + change.getFilename());
                printChangeScript(printStream, migrationsLoader.getScriptReader(change, false));
                printStream.println();
                printStream.println();
                printStream.println(generateVersionInsert(change, option));
                printStream.println();
            }
            return this;
        } catch (Exception e) {
            throw new MigrationException("Error executing command.  Cause: " + e, e);
        }
    }

    private List<Change> getPendingChanges(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader, DatabaseOperationOption option) {
        List<Change> pending = new ArrayList<Change>();
        List<Change> migrations = migrationsLoader.getMigrations();

        if (!changelogExists(connectionProvider, option)) {
            Collections.sort(migrations);
            return migrations;
        }

        List<Change> changelog = getChangelog(connectionProvider, option);
        for (Change change : migrations) {
            int index = changelog.indexOf(change);
            if (index < 0) {
                pending.add(change);
            }
        }
        Collections.sort(pending);
        return pending;
    }

    private String generateVersionInsert(Change change, DatabaseOperationOption option) {
        return "INSERT INTO " + option.getChangelogTable() + " (ID, APPLIED_AT, DESCRIPTION) " +
                "VALUES (" + change.getId() + ", '" + DatabaseOperation.generateAppliedTimeStampAsString() + "', '"
                + change.getDescription().replace('\'', ' ') + "')" + option.getDelimiter();
    }

    private void printChangeScript(PrintStream printStream, Reader migrationReader) throws IOException {
        char[] cbuf = new char[1024];
        int l;
        while ((l = migrationReader.read(cbuf)) == cbuf.length) {
            printStream.print(new String(cbuf, 0, l));
        }
        if (l > 0) {
            printStream.print(new String(cbuf, 0, l - 1));
        }
    }
}
