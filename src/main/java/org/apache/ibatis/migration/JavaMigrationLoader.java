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

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.io.ResolverUtil.Test;

public class JavaMigrationLoader implements MigrationLoader {

  private String[] packageNames;

  private ClassLoader classLoader;

  public JavaMigrationLoader(String... packageNames) {
    this(null, packageNames);
  }

  public JavaMigrationLoader(ClassLoader classLoader, String... packageNames) {
    super();
    this.classLoader = classLoader;
    this.packageNames = packageNames;
  }

  @Override
  public List<Change> getMigrations() {
    List<Change> migrations = new ArrayList<Change>();
    ResolverUtil<MigrationScript> resolver = getResolver(MigrationScript.class);
    resolver.findImplementations(MigrationScript.class, packageNames);
    Set<Class<? extends MigrationScript>> classes = resolver.getClasses();
    for (Class<? extends MigrationScript> clazz : classes) {
      try {
        if (!Modifier.isAbstract(clazz.getModifiers())) {
          MigrationScript script = clazz.newInstance();
          Change change = parseChangeFromMigrationScript(script);
          migrations.add(change);
        }
      } catch (Exception e) {
        throw new MigrationException("Could not instanciate MigrationScript: " + clazz.getName(), e);
      }
    }
    return migrations;
  }

  private Change parseChangeFromMigrationScript(MigrationScript script) {
    Change change = new Change();
    change.setId(script.getId());
    change.setDescription(script.getDescription());
    change.setFilename(script.getClass().getName());
    return change;
  }

  @Override
  public Reader getScriptReader(Change change, boolean undo) {
    ResolverUtil<MigrationScript> resolver = getResolver(MigrationScript.class);
    final String className = change.getFilename();
    for (String pkg : packageNames) {
      resolver.find(new Test() {
        @Override
        public boolean matches(Class<?> type) {
          return type != null && MigrationScript.class.isAssignableFrom(type) && type.getName().equals(className);
        }
      }, pkg);
    }
    Reader reader = null;
    Set<Class<? extends MigrationScript>> classes = resolver.getClasses();
    for (Class<? extends MigrationScript> clazz : classes) {
      try {
        MigrationScript script = clazz.newInstance();
        reader = new StringReader(undo ? script.getDownScript() : script.getUpScript());
      } catch (Exception e) {
        throw new MigrationException("Could not instanciate MigrationScript: " + clazz.getName(), e);
      }
      // There should be only one script.
      break;
    }
    return reader;
  }

  @Override
  public Reader getBootstrapReader() {
    return getSoleScriptReader(BootstrapScript.class);
  }

  @Override
  public Reader getOnAbortReader() {
    return getSoleScriptReader(OnAbortScript.class);
  }

  public <T extends SimpleScript> Reader getSoleScriptReader(Class<T> scriptClass) {
    ResolverUtil<T> resolver = getResolver(scriptClass);
    resolver.findImplementations(scriptClass, packageNames);
    Set<Class<? extends T>> classes = resolver.getClasses();
    if (classes == null || classes.isEmpty()) {
      return null;
    }
    if (classes.size() > 1) {
      throw new MigrationException("There can be only one implementation of " + scriptClass.getName());
    }
    Class<? extends T> clazz = classes.iterator().next();
    try {
      T script = clazz.newInstance();
      return new StringReader(script.getScript());
    } catch (Exception e) {
      throw new MigrationException("Could not instanciate script class: " + clazz.getName(), e);
    }
  }

  private <T> ResolverUtil<T> getResolver(Class<T> type) {
    ResolverUtil<T> resolver = new ResolverUtil<T>();
    if (classLoader != null) {
      resolver.setClassLoader(classLoader);
    }
    return resolver;
  }
}
