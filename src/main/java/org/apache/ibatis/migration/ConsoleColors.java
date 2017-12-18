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
package org.apache.ibatis.migration;

// Credit: https://stackoverflow.com/a/45444716

interface ConsoleColors {
  String RESET = "\033[0m";  // Text Reset

  // Regular Colors
  String BLACK = "\033[0;30m";   // BLACK
  String RED = "\033[0;31m";     // RED
  String GREEN = "\033[0;32m";   // GREEN
  String YELLOW = "\033[0;33m";  // YELLOW
  String BLUE = "\033[0;34m";    // BLUE
  String PURPLE = "\033[0;35m";  // PURPLE
  String CYAN = "\033[0;36m";    // CYAN
  String WHITE = "\033[0;37m";   // WHITE

  // Bold
  String BLACK_BOLD = "\033[1;30m";  // BLACK
  String RED_BOLD = "\033[1;31m";    // RED
  String GREEN_BOLD = "\033[1;32m";  // GREEN
  String YELLOW_BOLD = "\033[1;33m"; // YELLOW
  String BLUE_BOLD = "\033[1;34m";   // BLUE
  String PURPLE_BOLD = "\033[1;35m"; // PURPLE
  String CYAN_BOLD = "\033[1;36m";   // CYAN
  String WHITE_BOLD = "\033[1;37m";  // WHITE

  // Underline
  String BLACK_UNDERLINED = "\033[4;30m";  // BLACK
  String RED_UNDERLINED = "\033[4;31m";    // RED
  String GREEN_UNDERLINED = "\033[4;32m";  // GREEN
  String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
  String BLUE_UNDERLINED = "\033[4;34m";   // BLUE
  String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
  String CYAN_UNDERLINED = "\033[4;36m";   // CYAN
  String WHITE_UNDERLINED = "\033[4;37m";  // WHITE

  // Background
  String BLACK_BACKGROUND = "\033[40m";  // BLACK
  String RED_BACKGROUND = "\033[41m";    // RED
  String GREEN_BACKGROUND = "\033[42m";  // GREEN
  String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
  String BLUE_BACKGROUND = "\033[44m";   // BLUE
  String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
  String CYAN_BACKGROUND = "\033[46m";   // CYAN
  String WHITE_BACKGROUND = "\033[47m";  // WHITE

  // High Intensity
  String BLACK_BRIGHT = "\033[0;90m";  // BLACK
  String RED_BRIGHT = "\033[0;91m";    // RED
  String GREEN_BRIGHT = "\033[0;92m";  // GREEN
  String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
  String BLUE_BRIGHT = "\033[0;94m";   // BLUE
  String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
  String CYAN_BRIGHT = "\033[0;96m";   // CYAN
  String WHITE_BRIGHT = "\033[0;97m";  // WHITE

  // Bold High Intensity
  String BLACK_BOLD_BRIGHT = "\033[1;90m"; // BLACK
  String RED_BOLD_BRIGHT = "\033[1;91m";   // RED
  String GREEN_BOLD_BRIGHT = "\033[1;92m"; // GREEN
  String YELLOW_BOLD_BRIGHT = "\033[1;93m";// YELLOW
  String BLUE_BOLD_BRIGHT = "\033[1;94m";  // BLUE
  String PURPLE_BOLD_BRIGHT = "\033[1;95m";// PURPLE
  String CYAN_BOLD_BRIGHT = "\033[1;96m";  // CYAN
  String WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE

  // High Intensity backgrounds
  String BLACK_BACKGROUND_BRIGHT = "\033[0;100m";// BLACK
  String RED_BACKGROUND_BRIGHT = "\033[0;101m";// RED
  String GREEN_BACKGROUND_BRIGHT = "\033[0;102m";// GREEN
  String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m";// YELLOW
  String BLUE_BACKGROUND_BRIGHT = "\033[0;104m";// BLUE
  String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m"; // PURPLE
  String CYAN_BACKGROUND_BRIGHT = "\033[0;106m";  // CYAN
  String WHITE_BACKGROUND_BRIGHT = "\033[0;107m";   // WHITE
}
