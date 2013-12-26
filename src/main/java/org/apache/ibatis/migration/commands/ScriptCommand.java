package org.apache.ibatis.migration.commands;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
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

  public void execute(String... sparams) {
    try {
      if (sparams == null || sparams.length < 1 || sparams[0] == null) {
        throw new MigrationException("The script command requires a range of versions from v1 - v2.");
      }
      StringTokenizer parser = new StringTokenizer(sparams[0]);
      if (parser.countTokens() != 2) {
        throw new MigrationException("The script command requires a range of versions from v1 - v2.");
      }
      BigDecimal v1 = new BigDecimal(parser.nextToken());
      BigDecimal v2 = new BigDecimal(parser.nextToken());
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
    return "INSERT INTO " + changelogTable() + " (ID, APPLIED_AT, DESCRIPTION) " +
        "VALUES (" + change.getId() + ", '" + DatabaseOperation.generateAppliedTimeStampAsString() + "', '"
        + change.getDescription().replace('\'', ' ') + "')" + getDelimiter();
  }

  private String generateVersionDelete(Change change) {
    return "DELETE FROM " + changelogTable() + " WHERE ID = " + change.getId() + getDelimiter();
  }

  private boolean shouldRun(Change change, BigDecimal v1, BigDecimal v2) {
    BigDecimal id = change.getId();
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
