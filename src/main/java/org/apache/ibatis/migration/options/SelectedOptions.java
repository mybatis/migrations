/**
 *    Copyright 2010-2015 the original author or authors.
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

public class SelectedOptions {
  private SelectedPaths paths = new SelectedPaths();
  private String environment = "development";
  private String template;
  private boolean force;
  private boolean trace;
  private String command;
  private String params;
  private boolean help;

  public SelectedPaths getPaths() {
    return paths;
  }

  public void setPaths(SelectedPaths paths) {
    this.paths = paths;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String aEnvironment) {
    environment = aEnvironment;
  }

  public String getTemplate() {
    return template;
  }

  public void setTemplate(String aTemplate) {
    template = aTemplate;
  }

  public boolean isForce() {
    return force;
  }

  public void setForce(boolean aForce) {
    force = aForce;
  }

  public boolean isTrace() {
    return trace;
  }

  public void setTrace(boolean aTrace) {
    trace = aTrace;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String aCommand) {
    command = aCommand;
  }

  public String getParams() {
    return params;
  }

  public void setParams(String aParams) {
    params = aParams;
  }

  public boolean needsHelp() {
    return help;
  }

  public void setHelp(boolean aHelp) {
    help = aHelp;
  }
}
