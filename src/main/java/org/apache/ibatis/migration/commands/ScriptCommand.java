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
package org.apache.ibatis.migration.commands;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.operations.DatabaseOperation;
import org.apache.ibatis.migration.options.SelectedOptions;

public final class ScriptCommand extends BaseCommand {

  public ScriptCommand(SelectedOptions options) {
    super(options);
  }

  @Override
  public void execute(String... sparams) {
    try {
      if (sparams == null || sparams.length < 1 || sparams[0] == null) {
        throw new MigrationException("The script command requires a range of versions from v1 - v2.");
      }
      StringTokenizer parser = new StringTokenizer(sparams[0]);
      if (parser.countTokens() != 2) {
        throw new MigrationException("The script command requires a range of versions from v1 - v2.");
      }
      Long v1 = Long.valueOf(parser.nextToken());
      Long v2 = Long.valueOf(parser.nextToken());
      int comparison = v1.compareTo(v2);
      if (comparison == 0) {
        throw new MigrationException("The script command requires two different versions. Use 0 to include the first version.");
      }
      boolean undo = comparison > 0;
      List<Change> migrations = getMigrationLoader().getMigrations();
      Collections.sort(migrations);
      if (undo) {
        Collections.reverse(migrations);
      }
      for (Change change : migrations) {
        if (shouldRun(change, v1, v2)) {
          printStream.println("-- " + change.getFilename());
          Reader migrationReader = getMigrationLoader().getScriptReader(change, undo);
          char[] cbuf = new char[1024];
          int l;
          while ((l = migrationReader.read(cbuf)) == cbuf.length) {
            printStream.print(new String(cbuf, 0, l));
          }

          if (l > 0) {
            printStream.print(new String(cbuf, 0, l - 1));
          }
          printStream.println();
          printStream.println();
          printStream.println(undo ? generateVersionDelete(change) : generateVersionInsert(change));
          printStream.println();
        }
      }
    } catch (IOException e) {
      throw new MigrationException("Error generating script. Cause: " + e, e);
    }

  }

  private String generateVersionInsert(Change change) {
	String changelogInsert = getDatabaseOperationOption().getChangelogInsert();
	String values = change.getId() + ", '" + DatabaseOperation.generateAppliedTimeStampAsString() + "', '"
	        + change.getDescription().replace('\'', ' ') + "'";
    return changelogInsert.replace("${changelog}", getDatabaseOperationOption().getChangelogTable())
    		.replace("?,?,?", values) + getDelimiter();
  }

  private String generateVersionDelete(Change change) {
	String changelogDelete = getDatabaseOperationOption().getChangelogDelete();
    return changelogDelete.replace("${changelog}", getDatabaseOperationOption().getChangelogTable())
    		.replace("?", Long.toString(change.getId())) + getDelimiter();
  }

  private boolean shouldRun(Change change, Long v1, Long v2) {
    Long id = change.getId();
    if (v1.compareTo(v2) > 0) {
      return (id.compareTo(v2) > 0 && id.compareTo(v1) <= 0);
    } else {
      return (id.compareTo(v1) > 0 && id.compareTo(v2) <= 0);
    }
  }
  
  // Issue 699
  private String getDelimiter() {
    Properties props = environmentProperties();
    StringBuilder delimiter = new StringBuilder();
    if (Boolean.valueOf(props.getProperty("full_line_delimiter"))) delimiter.append("\n"); 
    delimiter.append(props.getProperty("delimiter", ";"));
    return delimiter.toString();
  }

}
