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
    ResolverUtil<BootstrapScript> resolver = getResolver(BootstrapScript.class);
    resolver.findImplementations(BootstrapScript.class, packageNames);
    Set<Class<? extends BootstrapScript>> classes = resolver.getClasses();
    if (classes == null || classes.isEmpty()) {
      return null;
    }
    if (classes.size() > 1) {
      throw new MigrationException("There can be only one BootstrapScript implementation.");
    }
    Reader reader = null;
    for (Class<? extends BootstrapScript> clazz : classes) {
      try {
        BootstrapScript script = clazz.newInstance();
        reader = new StringReader(script.getScript());
      } catch (Exception e) {
        throw new MigrationException("Could not instanciate BootstrapScript: " + clazz.getName(), e);
      }
      // There should be only one class.
      break;
    }
    return reader;
  }

  private <T> ResolverUtil<T> getResolver(Class<T> type) {
    ResolverUtil<T> resolver = new ResolverUtil<T>();
    if (classLoader != null) {
      resolver.setClassLoader(classLoader);
    }
    return resolver;
  }
}
