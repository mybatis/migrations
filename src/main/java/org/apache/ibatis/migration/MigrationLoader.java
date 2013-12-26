package org.apache.ibatis.migration;

import java.io.Reader;
import java.util.List;

public interface MigrationLoader {

  /**
   * @return A list of migrations (bootstrap should NOT be included).
   */
  List<Change> getMigrations();

  /**
   * @param change identifies the migration to read.
   * @param undo whether the caller requests UNDO SQL script or not.
   * @return A {@link Reader} of the specified SQL script.
   */
  Reader getScriptReader(Change change, boolean undo);

  /**
   * @return A {@link Reader} of the bootstrap SQL script.
   */
  Reader getBootstrapReader();

}
