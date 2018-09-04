/**
 *    Copyright 2010-2018 the original author or authors.
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.hook.MigrationHook;
import org.apache.ibatis.migration.hook.NewHookContext;
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
    String filename = getNextIDAsString() + "_" + description.replace(' ', '_') + ".sql";

    Map<String, Object> hookBindings = new HashMap<String, Object>();
    MigrationHook hook = createNewHook();
    if (hook != null) {
      hookBindings.put(MigrationHook.HOOK_CONTEXT, new NewHookContext(description, filename));
      hook.before(hookBindings);
    }

    if (options.getTemplate() != null) {
      copyExternalResourceTo(options.getTemplate(), Util.file(paths.getScriptPath(), filename), variables);
    } else {
      String customConfiguredTemplate = Util.getPropertyOption(CUSTOM_NEW_COMMAND_TEMPLATE_PROPERTY);
      if (customConfiguredTemplate != null && !customConfiguredTemplate.isEmpty()) {
        copyExternalResourceTo(Util.migrationsHome() + File.separator + customConfiguredTemplate,
            Util.file(paths.getScriptPath(), filename), variables);
      } else {
        printStream
            .append("Your migrations configuration did not find your custom template.  Using the default template.");
        copyDefaultTemplate(variables, filename);
      }
      if (hook != null) {
        hookBindings.put(MigrationHook.HOOK_CONTEXT, new NewHookContext(description, filename));
        hook.after(hookBindings);
      }
    }
    printStream.println("Done!");
    printStream.println();
  }

  private void copyDefaultTemplate(Properties variables, String filename) {
    copyResourceTo("org/apache/ibatis/migration/template_migration.sql", Util.file(paths.getScriptPath(), filename),
        variables);
  }

  private MigrationHook createNewHook() {
    String before = environment().getHookBeforeNew();
    String after = environment().getHookAfterNew();
    if (before == null && after == null) {
      return null;
    }
    return createFileMigrationHook(before, null, null, after);
  }
}
