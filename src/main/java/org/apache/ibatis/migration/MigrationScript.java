package org.apache.ibatis.migration;

import java.math.BigDecimal;

public interface MigrationScript {
  /**
   * @return ID of this migration script.<br>
   *         Newer script should have a larger ID number.
   */
  BigDecimal getId();

  /**
   * @return Short description of this migration script.
   */
  String getDescription();

  /**
   * @return SQL statement(s) executed at runtime schema upgrade.
   */
  String getUpScript();

  String getDownScript();
}
