MYBATIS Migrations
==================
[![build](https://github.com/mybatis/migrations/workflows/Java%20CI/badge.svg)](https://github.com/mybatis/migrations/actions?query=workflow%3A%22Java+CI%22)
[![Coverage Status](https://coveralls.io/repos/mybatis/migrations/badge.svg?branch=master&service=github)](https://coveralls.io/github/mybatis/migrations?branch=master)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/org.mybatis/mybatis-migrations/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.mybatis/mybatis-migrations)
[![Docker pulls](https://img.shields.io/docker/pulls/mybatis/migrations.svg)](https://hub.docker.com/r/mybatis/migrations)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/org.mybatis/mybatis-migrations.svg)](https://oss.sonatype.org/content/repositories/snapshots/org/mybatis/mybatis-migrations/)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

![mybatis-migrations](http://mybatis.github.io/images/mybatis-logo.png)

## Requirements
  MyBatis Migrations is a Java tool, so you must have Java installed in order to proceed. 
Users need at least the Java Runtime Environment (JRE), the Java Development Kit (JDK) is a plus. 
MyBatis Migrations requires Java version 8 or later.

See the [reference documentation](http://mybatis.github.io/migrations)

## Installation
|Bundle Locations|
|------|
|[Releases - https://oss.sonatype.org/content/repositories/releases/org/mybatis/mybatis-migrations](https://oss.sonatype.org/content/repositories/releases/org/mybatis/mybatis-migrations/)|
|[Snapshots - https://oss.sonatype.org/content/repositories/snapshots/org/mybatis/mybatis-migrations](https://oss.sonatype.org/content/repositories/snapshots/org/mybatis/mybatis-migrations/)|

#### Windows
1. Unzip the distribution archive, i.e. mybatis-${project.version}-migrations.zip to the directory you wish
      to install MyBatis Migrations.
      These instructions assume you chose C:\Program Files\mybatis.
      The subdirectory mybatis-migrations-${project.version} will be created from the archive.
1. Add the MIGRATIONS_HOME environment variable by opening up the system properties (WinKey + Pause), selecting the
      Advanced tab, and the Environment Variables button, then adding the MIGRATIONS_HOME variable in the user
      variables with the value C:\Program Files\mybatis\mybatis-migrations-${project.version}.
      Be sure to omit any quotation marks around the path even if it contains spaces.
1. In the same dialog, add the MIGRATIONS environment variable in the user variables with the
      value %MIGRATIONS_HOME%\bin.
1. In the same dialog, update/create the Path environment variable in the user variables and prepend the value
      %MIGRATIONS% to add MyBatis Migrations available in the command line.

#### Unix-based Operating Systems (Linux, Solaris and Mac OS X)
Download and extract migrations to any directory.
```sh
cd $HOME/opt
wget https://oss.sonatype.org/content/repositories/releases/org/mybatis/mybatis-migrations/3.3.5/mybatis-migrations-3.3.5-bundle.zip
unzip mybatis-migrations-3.3.5-bundle.zip
```

In your ~/.bashrc or ~/.zshrc or equivalent add
```
export MIGRATIONS=$HOME/opt/mybatis-migrations-3.3.5 # replace with path you extracted to
export PATH=$MIGRATIONS/bin:$PATH
```

## Quick setup
```sh
mkdir $HOME/my-migrations
cd $HOME/my-migrations
migrate init
```
After that read the ./drivers and ./environments section of [Migrations init](http://mybatis.github.io/migrations/init.html)

## docker
Docker users can use https://hub.docker.com/r/mybatis/migrations

## License

Migrations is [Apache Licensed](LICENSE)
