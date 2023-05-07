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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Clinton Begin
 */
public class VariableReplacer {

  private static final String OPEN_TOKEN = "${";
  private static final String CLOSE_TOKEN = "}";
  private final List<Map<? extends Object, ? extends Object>> variablesList;

  public VariableReplacer(Map<? extends Object, ? extends Object> variablesList) {
    this(Arrays.asList(variablesList));
  }

  public VariableReplacer(List<Map<? extends Object, ? extends Object>> variablesList) {
    this.variablesList = variablesList == null ? Collections.emptyList()
        : variablesList.stream().filter(Objects::nonNull).collect(Collectors.toList());
  }

  public String replace(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    // search open token
    int start = text.indexOf(OPEN_TOKEN);
    if (start == -1) {
      return text;
    }
    char[] src = text.toCharArray();
    int offset = 0;
    final StringBuilder builder = new StringBuilder();
    StringBuilder expression = null;
    while (start > -1) {
      if (start > 0 && src[start - 1] == '\\') {
        // this open token is escaped. remove the backslash and continue.
        builder.append(src, offset, start - offset - 1).append(OPEN_TOKEN);
        offset = start + OPEN_TOKEN.length();
      } else {
        // found open token. let's search close token.
        if (expression == null) {
          expression = new StringBuilder();
        } else {
          expression.setLength(0);
        }
        builder.append(src, offset, start - offset);
        offset = start + OPEN_TOKEN.length();
        int end = text.indexOf(CLOSE_TOKEN, offset);
        while (end > -1) {
          if (end <= offset || src[end - 1] != '\\') {
            expression.append(src, offset, end - offset);
            break;
          }
          // this close token is escaped. remove the backslash and continue.
          expression.append(src, offset, end - offset - 1).append(CLOSE_TOKEN);
          offset = end + CLOSE_TOKEN.length();
          end = text.indexOf(CLOSE_TOKEN, offset);
        }
        if (end == -1) {
          // close token was not found.
          builder.append(src, start, src.length - start);
          offset = src.length;
        } else {
          appendWithReplace(builder, expression.toString());
          offset = end + CLOSE_TOKEN.length();
        }
      }
      start = text.indexOf(OPEN_TOKEN, offset);
    }
    if (offset < src.length) {
      builder.append(src, offset, src.length - offset);
    }
    return builder.toString();
  }

  private StringBuilder appendWithReplace(StringBuilder builder, String key) {
    String value = null;
    for (Map<? extends Object, ? extends Object> variables : variablesList) {
      value = (String) variables.get(key);
      if (value != null) {
        builder.append(value);
        break;
      }
    }
    if (value == null) {
      builder.append(OPEN_TOKEN).append(key).append(CLOSE_TOKEN);
    }
    return builder;
  }
}
