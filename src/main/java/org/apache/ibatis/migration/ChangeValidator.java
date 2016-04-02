package org.apache.ibatis.migration;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.regex.Pattern;

public class ChangeValidator {
  private static final String CUSTOM_FILE_NAME_FILTER_PROPERTY = "filename_filter";

  /**
   * @param change to validate
   * @param properties Environmental configuration
   * @throws MigrationException
   */
  public static void validateChangeForConfiguration(Change change, Properties properties)
    throws MigrationException {
  String filename = change.getFilename();
    String filenameFilter = properties.getProperty(CUSTOM_FILE_NAME_FILTER_PROPERTY);
    if (filenameFilter != null) {
      Pattern p = null;
      try {
      p = Pattern.compile(filenameFilter, Pattern.CASE_INSENSITIVE);
      } catch (Exception ex) {
      throw new MigrationException("Exception parsing the value in your environmental configuration for the filename filter of " + filenameFilter, ex);
      }
    if (!p.matcher(filename).find()) {
      throw new MigrationException("The change filename " + filename +  " does not match the required filename filter of " + filenameFilter);
    }
    }
  }

  /**
   * @param filename
   * @param properties Environmental configuration
   * @return Change
   */
  public static Change parseChangeFromFilename(String filename, Properties properties) {
    try {
      Change change = new Change();
      int lastIndexOfDot = filename.lastIndexOf(".");
      String[] parts = filename.substring(0, lastIndexOfDot).split("_");
      change.setId(new BigDecimal(parts[0]));
      StringBuilder builder = new StringBuilder();
      for (int i = 1; i < parts.length; i++) {
        if (i > 1) {
          builder.append(" ");
        }
        builder.append(parts[i]);
      }
      change.setDescription(builder.toString());
      change.setFilename(filename);
      return change;
    } catch (Exception e) {
      throw new MigrationException("Error parsing change from filename.  Cause: " + e, e);
    }
  }
}
