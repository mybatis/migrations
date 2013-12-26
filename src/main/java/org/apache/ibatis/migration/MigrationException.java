package org.apache.ibatis.migration;

import org.apache.ibatis.exceptions.PersistenceException;

public class MigrationException extends PersistenceException {

  private static final long serialVersionUID = 491769430730827896L;

  public MigrationException() {
    super();
  }

  public MigrationException(String message) {
    super(message);
  }

  public MigrationException(String message, Throwable cause) {
    super(message, cause);
  }

  public MigrationException(Throwable cause) {
    super(cause);
  }
}
