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

import java.io.File;

public enum Options {

  PATH {
    @Override
    public void accept(SelectedOptions options, String arg) {
      options.getPaths().setBasePath(new File(arg));
    }
  },
  ENVPATH {
    @Override
    public void accept(SelectedOptions options, String arg) {
      options.getPaths().setEnvPath(new File(arg));
    }
  },
  SCRIPTPATH {
    @Override
    public void accept(SelectedOptions options, String arg) {
      options.getPaths().setScriptPath(new File(arg));
    }
  },
  DRIVERPATH {
    @Override
    public void accept(SelectedOptions options, String arg) {
      options.getPaths().setDriverPath(new File(arg));
    }
  },
  HOOKPATH {
    @Override
    public void accept(SelectedOptions options, String arg) {
      options.getPaths().setHookPath(new File(arg));
    }
  },
  ENV {
    @Override
    public void accept(SelectedOptions options, String arg) {
      options.setEnvironment(arg);
    }
  },
  FORCE {
    @Override
    public void accept(SelectedOptions options, String ignored) {
      options.setForce(true);
    }
  },
  TRACE {
    @Override
    public void accept(SelectedOptions options, String ignored) {
      options.setTrace(true);
    }
  },
  HELP {
    @Override
    /**
     * @ignored could used used to get help on a certain topic if implemented
     */
    public void accept(SelectedOptions options, String ignored) {
      options.setHelp(true);
    }
  },
  TEMPLATE {
    @Override
    public void accept(SelectedOptions options, String arg) {
      options.setTemplate(arg);
    }
  },
  IDPATTERN {
    @Override
    public void accept(SelectedOptions options, String arg) {
      options.setIdPattern(arg);
    }
  },
  QUIET {
    @Override
    public void accept(SelectedOptions options, String arg) {
      options.setQuiet(true);
    }
  };
  public abstract void accept(SelectedOptions options, String arg);

}
