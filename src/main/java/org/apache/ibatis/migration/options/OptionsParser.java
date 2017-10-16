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
package org.apache.ibatis.migration.options;

import static org.apache.ibatis.migration.utils.Util.isOption;

import java.io.File;

public enum OptionsParser {
  ;

  public static SelectedOptions parse(String[] args) {
    final SelectedOptions selectedOptions = new SelectedOptions();

    for (String arg : args) {
      final boolean isOption = isOption(arg);
      if (isOption) {
        parseOptions(arg, selectedOptions);
      } else {
        setCommandOrAppendParams(arg, selectedOptions);
      }
    }

    return selectedOptions;
  }

  private static void setCommandOrAppendParams(String arg, SelectedOptions options) {
    if (options.getCommand() == null) {
      options.setCommand(arg);
    } else {
      final String myParams = options.getParams() == null ? arg : options.getParams() + " " + arg;
      options.setParams(myParams);
    }
  }

  private static boolean parseOptions(String arg, SelectedOptions options) {
    final boolean isOption = isOption(arg);

    if (isOption) {
      final String[] argParts = arg.substring(2).split("=");
      final Options option = Options.valueOf(argParts[0].toUpperCase());

      option.accept(options, argParts.length > 1 ? argParts[1] : null);
    }

    return isOption;
  }
}
