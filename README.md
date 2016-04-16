MYBATIS Migrations
==================

[![Build Status](https://travis-ci.org/mybatis/migrations.svg?branch=master)](https://travis-ci.org/mybatis/migrations)
[![Coverage Status](https://coveralls.io/repos/mybatis/migrations/badge.svg?branch=master&service=github)](https://coveralls.io/github/mybatis/migrations?branch=master)
[![Dependency Status](https://www.versioneye.com/user/projects/5619ae16a193340f2f000505/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5619ae16a193340f2f000505)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/org.mybatis/mybatis-migrations/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.mybatis/mybatis-migrations)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

![mybatis-migrations](http://mybatis.github.io/images/mybatis-logo.png)

Install MyBatis Migrations ${project.version} (${implementation.build})

  MyBatis Migrations is a Java tool, so you must have Java installed in order to proceed. Users need at least the
  Java Runtime Environment (JRE), the Java Development Kit (JDK) is a plus.

  Additional optional installation steps are listed after the platform specific instructions.
  
  See the reference documentation here http://mybatis.github.io/migrations/

* Windows

  [1] Unzip the distribution archive, i.e. mybatis-${project.version}-migrations.zip to the directory you wish
      to install MyBatis Migrations.
      These instructions assume you chose C:\Program Files\mybatis.
      The subdirectory mybatis-migrations-${project.version} will be created from the archive.

  [2] Add the MIGRATIONS_HOME environment variable by opening up the system properties (WinKey + Pause), selecting the
      Advanced tab, and the Environment Variables button, then adding the MIGRATIONS_HOME variable in the user
      variables with the value C:\Program Files\mybatis\mybatis-migrations-${project.version}.
      Be sure to omit any quotation marks around the path even if it contains spaces.

  [3] In the same dialog, add the MIGRATIONS environment variable in the user variables with the
      value %MIGRATIONS_HOME%\bin.

  [4] In the same dialog, update/create the Path environment variable in the user variables and prepend the value
      %MIGRATIONS% to add MyBatis Migrations available in the command line.

* Unix-based Operating Systems (Linux, Solaris and Mac OS X)

  [1] Extract the distribution archive, i.e. mybatis-${project.version}-migrations.zip to the directory you wish to
      install MyBatis Migrations. These instructions assume you chose
      /usr/local/mybatis/mybatis-migrations-${project.version}.
      The subdirectory mybatis-migrations-${project.version} will be created from the archive.

  [2] In a command terminal, add the MIGRATIONS_HOME environment variable,
      e.g. export MIGRATIONS_HOME=/usr/local/mybatis/mybatis-migrations-${project.version}.

  [3] Add the MIGRATIONS environment variable, e.g. export MIGRATIONS=$MIGRATIONS_HOME/bin.

  [4] Add MIGRATIONS environment variable to your path, e.g. export PATH=$MIGRATIONS:$PATH.
