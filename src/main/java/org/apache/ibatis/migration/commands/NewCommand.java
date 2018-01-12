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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.hook.Hook;
import org.apache.ibatis.migration.options.SelectedOptions;

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


    Hook hook = createNewMigrationHook();

    Properties variables = getVariables();
    variables.setProperty("description", params[0]);


    Map<String, Object> hookBindings = createBinding(params);
    {

      Reader templateReader = getTemplateReader();
      Change change = (Change) hookBindings.get("change");
      hook.before(hookBindings);
      File changeFile = new File(change.getFilename());
      try {
        copyTemplate(templateReader, changeFile, variables);
      } catch (IOException e) {
        throw new MigrationException("Unable to create template file " + changeFile.getAbsolutePath());
      }

      hook.after(hookBindings);
    }
    printStream.println("Done!");
    printStream.println();

  }

  private Reader getTemplateReader() {
    String def = "org/apache/ibatis/migration/template_migration.sql";
    Reader templateReader = null;
    try {
      templateReader = Resources.getResourceAsReader(def);
    } catch (IOException e) {
      throw new MigrationException(String.format("Default template %s can't be found?! ", def), e);
    }
    try {
      String template = getTemplateFile();
      if (template != null)
        templateReader = new FileReader(template);

    } catch (FileNotFoundException e) {
      String msg = String.format(
          "Your migrations configuration did not find your custom template: %s.  Using the default template.",
          e.getMessage());
      printStream.append(msg);
    }
    return templateReader;
  }

  private String getTemplateFile() throws FileNotFoundException {
    String template = null;
    if (options.getTemplate() != null) {
      template = options.getTemplate();
    } else {
      String customConfiguredTemplate = getPropertyOption(CUSTOM_NEW_COMMAND_TEMPLATE_PROPERTY);
      if (customConfiguredTemplate != null) {
        template = migrationsHome() + "/" + customConfiguredTemplate;
      }
    }
    return template;
  }

  private Map<String, Object> createBinding(String[] params) {
    String nextId = getNextIDAsString();
    String description = params[0];

    String proposedFile = nextId + "_" + description.replace(' ', '_') + ".sql";

    Map<String, Object> hookBindings = new HashMap<String, Object>();
    BigDecimal id = new BigDecimal(nextId);

    File f = new File(String.format("%s%s%s", paths.getScriptPath(), File.separator, proposedFile));
    Change change = new Change(id, null, description, f.getAbsolutePath());
    Properties props = environmentProperties();

    hookBindings.put("change", change);
    hookBindings.put("paths", paths);
    hookBindings.put("environment", props);
    hookBindings.put("args", Arrays.asList(params));
    return hookBindings;
  }

}
