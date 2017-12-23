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

import static org.apache.ibatis.migration.hook.MigrationHook.HOOK_CONTEXT;
import static org.apache.ibatis.migration.operations.DatabaseOperation.getScriptRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.hook.HookContext;
import org.apache.ibatis.migration.hook.MigrationHook;
import org.apache.ibatis.migration.options.DatabaseOperationOption;
import org.apache.ibatis.migration.options.SelectedOptions;
import org.apache.ibatis.migration.utils.Util;

public final class NewCommand extends BaseCommand {

  private static final String CUSTOM_NEW_COMMAND_TEMPLATE_PROPERTY = "new_command.template";

  public NewCommand(SelectedOptions options) {
    super(options);
  }

  @Override
  public void execute(String... params) {
    if (paramsEmpty(params)) {
      throw new MigrationException("No description specified for new migration.");
    }
    String description = params[0];
    Properties variables = new Properties();
    variables.setProperty("description", description);
    existingEnvironmentFile();
    String nextId = getNextIDAsString();
    String filename = nextId + "_" + description.replace(' ', '_') + ".sql";

    File templateCopy = Util.file(paths.getScriptPath(), filename);
    ConnectionProvider connectionProvider = getConnectionProvider();
    DatabaseOperationOption option = getDatabaseOperationOption();
    ScriptRunner runner = getScriptRunner(connectionProvider, option, printStream);

    MigrationHook hook = createNewHook();
    Map<String, Object> hookBindings = new HashMap<String, Object>();
    Change change = new Change(new BigDecimal(nextId), new Date().toString(), description,
        templateCopy.getAbsolutePath());

    hookBindings.put(HOOK_CONTEXT, new HookContext(connectionProvider, runner, change));

    if (options.getTemplate() != null) {
      hook.before(hookBindings);
      copyExternalResourceTo(options.getTemplate(), templateCopy, variables);
    } else {
      try {
        String customConfiguredTemplate = getPropertyOption(CUSTOM_NEW_COMMAND_TEMPLATE_PROPERTY);
        if (customConfiguredTemplate != null) {
          hook.before(hookBindings);
          copyExternalResourceTo(migrationsHome() + "/" + customConfiguredTemplate, templateCopy, variables);
        } else {
          change.setFilename(filename);
          hook.before(hookBindings);
          copyDefaultTemplate(variables, filename);
        }
      } catch (FileNotFoundException e) {
        printStream
            .append("Your migrations configuration did not find your custom template.  Using the default template.");
        copyDefaultTemplate(variables, filename);
      }
    }

    hook.after(hookBindings);

    printStream.println("Done!");
    printStream.println();

  }

  private void copyDefaultTemplate(Properties variables, String filename) {
    copyResourceTo("org/apache/ibatis/migration/template_migration.sql", Util.file(paths.getScriptPath(), filename),
        variables);
  }
}
