/**
 *    Copyright 2010-2016 the original author or authors.
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
import org.apache.ibatis.migration.utils.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

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
    String filename = getNextIDAsString() + "_" + description.replace(' ', '_') + ".sql";
    String fileTemplate = options.getFileTemplate();
    File fileTemplateFile = null;
    if (fileTemplate != null) {
      fileTemplateFile = Util.file(paths.getReferencedFilesPath(), fileTemplate);
      if (!fileTemplateFile.exists()) {
        try {
          fileTemplateFile.createNewFile();
        } catch (IOException e) {
          //nothing
        }
      }
    }

    if (options.getTemplate() != null) {
      copyExternalResourceTo(options.getTemplate(), Util.file(paths.getScriptPath(), filename), fileTemplateFile);
    } else {
      try {
        String customConfiguredTemplate = getPropertyOption(CUSTOM_NEW_COMMAND_TEMPLATE_PROPERTY);
        if (customConfiguredTemplate != null) {
          copyExternalResourceTo(migrationsHome() + "/" + customConfiguredTemplate,
            Util.file(paths.getScriptPath(), filename), fileTemplateFile);
        } else {
          copyDefaultTemplate(variables, filename, fileTemplateFile);
        }
      } catch (FileNotFoundException e) {
        printStream.append(
          "Your migrations configuration did not find your custom template.  Using the default template.");
        copyDefaultTemplate(variables, filename, fileTemplateFile);
      }
    }
    printStream.println("Done!");
    printStream.println();
  }

  private void copyDefaultTemplate(Properties variables, String filename, File fileTemplate) {
    copyResourceTo("org/apache/ibatis/migration/template_migration.sql",
      Util.file(paths.getScriptPath(), filename),
      variables, fileTemplate);
  }
}
