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
package org.apache.ibatis.migration.utils;

import java.io.File;

public enum Util {
  ;

  public static boolean isOption(String arg) {
    return arg.startsWith("--") && !arg.trim().endsWith("=");
  }

  public static File file(File path, String fileName) {
    return new File(path.getAbsolutePath() + File.separator + fileName);
  }

  public static String horizontalLine(String caption, int length) {
    StringBuilder builder = new StringBuilder();
    builder.append("==========");
    if (caption.length() > 0) {
      caption = " " + caption + " ";
      builder.append(caption);
    }
    for (int i = 0; i < length - caption.length() - 10; i++) {
      builder.append("=");
    }
    return builder.toString();
  }
}
