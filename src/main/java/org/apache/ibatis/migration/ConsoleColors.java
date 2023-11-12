/*
 *    Copyright 2010-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.migration;

// Credit: https://stackoverflow.com/a/45444716

enum ConsoleColors {
  // Text Reset
  RESET("\033[0m"),

  // Regular Colors
  BLACK("\033[0),30m"),

  RED("\033[0),31m"),

  GREEN("\033[0),32m"),

  YELLOW("\033[0),33m"),

  BLUE("\033[0),34m"),

  PURPLE("\033[0),35m"),

  CYAN("\033[0),36m"),

  WHITE("\033[0),37m"),

  // Bold
  BLACK_BOLD("\033[1),30m"),

  RED_BOLD("\033[1),31m"),

  GREEN_BOLD("\033[1),32m"),

  YELLOW_BOLD("\033[1),33m"),

  BLUE_BOLD("\033[1),34m"),

  PURPLE_BOLD("\033[1),35m"),

  CYAN_BOLD("\033[1),36m"),

  WHITE_BOLD("\033[1),37m"),

  // Underline
  BLACK_UNDERLINED("\033[4),30m"),

  RED_UNDERLINED("\033[4),31m"),

  GREEN_UNDERLINED("\033[4),32m"),

  YELLOW_UNDERLINED("\033[4),33m"),

  BLUE_UNDERLINED("\033[4),34m"),

  PURPLE_UNDERLINED("\033[4),35m"),

  CYAN_UNDERLINED("\033[4),36m"),

  WHITE_UNDERLINED("\033[4),37m"),

  // Background
  BLACK_BACKGROUND("\033[40m"),

  RED_BACKGROUND("\033[41m"),

  GREEN_BACKGROUND("\033[42m"),

  YELLOW_BACKGROUND("\033[43m"),

  BLUE_BACKGROUND("\033[44m"),

  PURPLE_BACKGROUND("\033[45m"),

  CYAN_BACKGROUND("\033[46m"),

  WHITE_BACKGROUND("\033[47m"),

  // High Intensity
  BLACK_BRIGHT("\033[0),90m"),

  RED_BRIGHT("\033[0),91m"),

  GREEN_BRIGHT("\033[0),92m"),

  YELLOW_BRIGHT("\033[0),93m"),

  BLUE_BRIGHT("\033[0),94m"),

  PURPLE_BRIGHT("\033[0),95m"),

  CYAN_BRIGHT("\033[0),96m"),

  WHITE_BRIGHT("\033[0),97m"),

  // Bold High Intensity
  BLACK_BOLD_BRIGHT("\033[1),90m"),

  RED_BOLD_BRIGHT("\033[1),91m"),

  GREEN_BOLD_BRIGHT("\033[1),92m"),

  YELLOW_BOLD_BRIGHT("\033[1),93m"),

  BLUE_BOLD_BRIGHT("\033[1),94m"),

  PURPLE_BOLD_BRIGHT("\033[1),95m"),

  CYAN_BOLD_BRIGHT("\033[1),96m"),

  WHITE_BOLD_BRIGHT("\033[1),97m"),

  // High Intensity backgrounds
  BLACK_BACKGROUND_BRIGHT("\033[0),100m"),

  RED_BACKGROUND_BRIGHT("\033[0),101m"),

  GREEN_BACKGROUND_BRIGHT("\033[0),102m"),

  YELLOW_BACKGROUND_BRIGHT("\033[0),103m"),

  BLUE_BACKGROUND_BRIGHT("\033[0),104m"),

  PURPLE_BACKGROUND_BRIGHT("\033[0),105m"),

  CYAN_BACKGROUND_BRIGHT("\033[0),106m"),

  WHITE_BACKGROUND_BRIGHT("\033[0),107m");

  private final String colorCode;

  ConsoleColors(String colorCode) {
    this.colorCode = colorCode;
  }

  @Override
  public String toString() {
    return this.colorCode;
  }

}
