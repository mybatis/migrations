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
package org.apache.ibatis.migration.hook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.options.SelectedPaths;
import org.apache.ibatis.migration.utils.Util;

public class Jsr223HookScript implements HookScript {

  private static final String MIGRATION_PATHS = "migrationPaths";

  private static final String KEY_FUNCTION = "_function";
  private static final String KEY_OBJECT = "_object";
  private static final String KEY_METHOD = "_method";
  private static final String KEY_ARG = "_arg";

  protected final String language;
  protected final File scriptFile;
  protected final String charset;
  protected final Properties variables;
  protected final SelectedPaths paths;
  protected final PrintStream printStream;

  protected String functionName;
  protected String objectName;
  protected String methodName;
  protected List<String> args = new ArrayList<String>();
  protected Map<String, String> localVars = new HashMap<String, String>();

  public Jsr223HookScript(String language, File scriptFile, String charset, String[] options, SelectedPaths paths,
      Properties variables, PrintStream printStream) {
    super();
    this.language = language;
    this.scriptFile = scriptFile;
    this.charset = charset;
    this.paths = paths;
    this.variables = variables;
    this.printStream = printStream;
    for (String option : options) {
      int sep = option.indexOf('=');
      if (sep > -1) {
        String key = option.substring(0, sep);
        String value = option.substring(sep + 1);
        if (KEY_FUNCTION.equals(key)) {
          functionName = value;
        } else if (KEY_METHOD.equals(key)) {
          methodName = value;
        } else if (KEY_OBJECT.equals(key)) {
          objectName = value;
        } else if (KEY_ARG.equals(key)) {
          args.add(value);
        } else {
          localVars.put(key, value);
        }
      }
    }
  }

  @Override
  public void execute(Map<String, Object> bindingMap) {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName(language);
    // bind global/local variables defined in the environment file
    Bindings bindings = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
    bindVariables(bindingMap, variables.entrySet());
    bindVariables(bindingMap, localVars.entrySet());
    bindings.put(MIGRATION_PATHS, paths);
    bindings.putAll(bindingMap);
    try {
      printStream.println(Util.horizontalLine("Applying JSR-223 hook : " + scriptFile.getName(), 80));
      engine.eval(new InputStreamReader(new FileInputStream(scriptFile), charset));
      if (functionName != null || (objectName != null && methodName != null)) {
        Invocable invocable = (Invocable) engine;
        if (functionName != null) {
          printStream.println(Util.horizontalLine("Invoking function : " + functionName, 80));
          invocable.invokeFunction(functionName, args.toArray());
        } else if (objectName != null && methodName != null) {
          printStream.println(Util.horizontalLine("Invoking method : " + methodName, 80));
          Object targetObject = engine.get(objectName);
          invocable.invokeMethod(targetObject, methodName, args.toArray());
        }
      }
      // store vars in bindings to the per-operation map
      bindVariables(bindingMap, bindings.entrySet());
    } catch (ClassCastException e) {
      throw new MigrationException(
          "Script engine '" + engine.getClass().getName() + "' does not support function/method invocation.", e);
    } catch (IOException e) {
      throw new MigrationException("Failed to read JSR-223 hook script file.", e);
    } catch (ScriptException e) {
      throw new MigrationException("Failed to execute JSR-223 hook script.", e);
    } catch (NoSuchMethodException e) {
      throw new MigrationException("Method or function not found in JSR-223 hook script: " + functionName, e);
    }
  }

  private <S, T> void bindVariables(Map<String, Object> bindingMap, Set<Entry<S, T>> vars) {
    for (Entry<S, T> entry : vars) {
      bindingMap.put((String) entry.getKey(), entry.getValue());
    }
  }
}
