/**
 *    Copyright 2010-2017 the original author or authors.
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

import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.options.SelectedOptions;

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
    switch (aResolvedCommand) {
      case INFO:
        return new InfoCommand(System.out);
      case INIT:
        return new InitializeCommand(selectedOptions);
      case BOOTSTRAP:
        return new BootstrapCommand(selectedOptions);
      case NEW:
        return new NewCommand(selectedOptions);
      case UP:
        return new UpCommand(selectedOptions);
      case DOWN:
        return new DownCommand(selectedOptions);
      case PENDING:
        return new PendingCommand(selectedOptions);
      case SCRIPT:
        return new ScriptCommand(selectedOptions);
      case VERSION:
        return new VersionCommand(selectedOptions);
      case STATUS:
        return new StatusCommand(selectedOptions);
      default:
        return new Command() {
          @Override
          public void execute(String... params) {
            System.out.println("unknown command");
          }
        };
    }
  }
}
