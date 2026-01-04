MYBATIS Migrations
==================
[![Java CI](https://github.com/mybatis/migrations/actions/workflows/ci.yaml/badge.svg)](https://github.com/mybatis/migrations/actions/workflows/ci.yaml)
[![Coverage Status](https://coveralls.io/repos/github/mybatis/migrations/badge.svg?branch=master)](https://coveralls.io/github/mybatis/migrations?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/org.mybatis/mybatis-migrations)](https://central.sonatype.com/artifact/org.mybatis/mybatis-migrations)
[![Docker pulls](https://img.shields.io/docker/pulls/mybatis/migrations)](https://hub.docker.com/r/mybatis/migrations)
[![License](https://img.shields.io/github/license/mybatis/migrations)](https://www.apache.org/licenses/LICENSE-2.0)

![mybatis-migrations](https://mybatis.org/images/mybatis-logo.png)

## Requirements
- MyBatis Migrations is a Java tool, so you must have Java installed in order to proceed. 
- Users need at least the Java Runtime Environment (JRE), the Java Development Kit (JDK) is a plus. 
- MyBatis Migrations requires Java version 11 or later.

See the [reference documentation](https://mybatis.org/migrations)

## Installation
|Bundle Locations|
|------|
|[Releases - https://repo1.maven.org/maven2/org/mybatis/mybatis-migrations/](https://repo1.maven.org/maven2/org/mybatis/mybatis-migrations/)|
|[Releases – https://github.com/mybatis/migrations/releases](https://github.com/mybatis/migrations/releases)|

#### Windows
1. Unzip the distribution archive, i.e. ```mybatis-${project.version}-migrations.zip``` to the directory you wish
      to install MyBatis Migrations.
      These instructions assume you chose ```C:\Program Files\mybatis```.
      The subdirectory ```mybatis-migrations-${project.version}``` will be created from the archive.
1. Add the ``MIGRATIONS_HOME`` environment variable by opening up the system properties (WinKey + Pause), selecting the
      Advanced tab, and the Environment Variables button, then adding the ```MIGRATIONS_HOME``` variable in the user
      variables with the value ```C:\Program Files\mybatis\mybatis-migrations-${project.version}```.
      Be sure to omit any quotation marks around the path even if it contains spaces.
1. In the same dialog, add the ```MIGRATIONS``` environment variable in the user variables with the
      value ```%MIGRATIONS_HOME%\bin```.
1. In the same dialog, update/create the Path environment variable in the user variables and prepend the value
      ```%MIGRATIONS%``` to add MyBatis Migrations available in the command line.

#### Unix-based Operating Systems (Linux, Solaris and Mac OS X)
Download and extract migrations to any directory.
```sh
cd $HOME/opt
wget https://repo1.maven.org/maven2/org/mybatis/mybatis-migrations/3.5.0/mybatis-migrations-3.5.0-bundle.zip (or wget https://github.com/mybatis/migrations/releases/download/mybatis-migrations-3.5.0/mybatis-migrations-3.5.0-bundle.zip)
unzip mybatis-migrations-3.5.0-bundle.zip
```

In your ~/.bashrc or ~/.zshrc or equivalent add
```
export MIGRATIONS=$HOME/opt/mybatis-migrations-3.5.0 # replace with path you extracted to
export PATH=$MIGRATIONS/bin:$PATH
```

## Quick setup
```sh
mkdir $HOME/my-migrations
cd $HOME/my-migrations
migrate init
```
After that read the ./drivers and ./environments section of [Migrations init](https://mybatis.org/migrations/init.html)

## Docker
Docker users can use https://hub.docker.com/r/mybatis/migrations

## Package Manager ##

*Note*: These are not maintained by the MyBatis team and issues should be reported to the package maintainers.

### SDKMAN ###

[SDKMAN](https://sdkman.io) is a tool to manage multiple installations of JDKs and SDKs. MyBatis Migrations is available as a [candidate in SDKMAN](https://sdkman.io/sdks#mybatis). To install using SKDMAN

```sh
sdk install mybatis

# you can list the available versions
sdk ls mybatis

# install specific version
sdk install mybatis 3.5.0
```

## License

Migrations is [Apache Licensed](LICENSE)
