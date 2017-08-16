[![Build Status](https://qa.nuxeo.org/jenkins/buildStatus/icon?job=Misc/tools_nuxeo-studio-maven-plugin-master)](https://qa.nuxeo.org/jenkins/job/Misc/job/tools_nuxeo-studio-maven-plugin-master)

# Introduction

This plugin allows you to extract contributions from any Nuxeo Project.

The `ExtractorMojo` provides the entry point to extract what is contributed in the target project.

## Goals Overview

The available plugin goal is:

- `nuxeo-studio:extract`: execute an extract inherited from all submodules of the contributions. Depending of the `nsmp.token` parameter, JSON output is written in the `nsmp.output` file path or pushed to Nuxeo Studio.
- `nuxeo-studio:help`: display Mojo help message

# Usage

Extract contributions from your project:

```bash
mvn [-o] [-DskipTests] [-DskipITs] [package] org.nuxeo.tools:nuxeo-studio-maven-plugin:extract
cat target/nuxeo-studio-registries.json
```

Publish contributions in Studio Registries:

```bash
mvn [-o] [-DskipTests] [-DskipITs] [package] org.nuxeo.tools:nuxeo-studio-maven-plugin:extract -Dnsmp.symbolicName="MY-PROJECT" -Dnsmp.token="MY-TOKEN"
```

Online documentation:

TODO

## Parameters

- **connectUrl** (Default: `https://connect.nuxeo.com/nuxeo/site`): Nuxeo Connect URL

   User property: `nsmp.connectUrl`

- **extract** (Default: `*`): list of contributions registries exported. Multiple values must be
  separate using a coma.

   Possible Values:
    - operations
    - doctypes
    - schemas
    - facets
    - events
    - permissions
    - lifecycles
    - *

   User property: `nsmp.extract`

- **output** (Default: `nuxeo-studio-registries.json`): File output name. JSON Registries will be written in this file in the `output` directory.

   User property: `nsmp.output`

- **symbolicName**: Nuxeo Studio Target Project Symbolic Name

   User property: `nsmp.symbolicName`

- **token**: Studio Authentication Token

   User property: `nsmp.token`

## Thread safety

Different Mojo instances can be used in different threads, each of them will
have its own graph. (The Mojo is bound to a thread variable so that Ant will
use the Mojo bound to the current thread).

# How to contribute

See this page for practical information:
[NXDOC/Contributing+to+Nuxeo](http://doc.nuxeo.com/x/VIZH)

This presentation will give you more insight about "the Nuxeo way":
<http://www.slideshare.net/nuxeo/nuxeo-world-session-becoming-a-contributor-how-to-get-started>

## QA results

[![Build Status](https://qa.nuxeo.org/jenkins/buildStatus/icon?job=tools_ant-assembly-maven-plugin-master)](https://qa.nuxeo.org/jenkins/job/tools_ant-assembly-maven-plugin-master/)

## Build and tests

    mvn clean package [-o] [-DskipTests] [-DskipITs] [-DdebugITs=true] [-Dinvoker.test=...] [-Pdebug]

### Build and run all Unit and integration tests

    mvn clean integration-test [-o] [-DdebugITs=true]

### Build with no test

    mvn clean package -DskipTests -DskipITs

### Run only some integration tests

    mvn invoker:run -Dinvoker.test=test1

or (if you want the code being compiled again):

    mvn clean integration-test -Dinvoker.test=test1

Use comma separator. Wildcards are accepted.

### Integration tests results

Results are in `target/it/*` sub-folders.

### Manually replay integration tests

You can manually execute some Maven commands from a result directory (`target/it/*`) providing a specific `settings.xml` file:

    mvn -s ../../../it-settings.xml ...

### Integration tests debug logs

Add `-DdebugITs=true` on the command line to get debug logs in the build.log file.

### Integration tests debug code (mvnDebug)

Use `-Pdebug` profile which will make the invoker call mvnDebug (starting the JVM in debug attach mode).

Default listening for transport dt_socket at address: 8000

# About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at www.nuxeo.com.
