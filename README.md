# NightlyTestsPlugin

![example workflow](https://github.com/VISUS-Health-IT-GmbH/NightlyTestsPlugin/actions/workflows/gradle.yml/badge.svg)
![example workflow](https://github.com/VISUS-Health-IT-GmbH/NightlyTestsPlugin/actions/workflows/gradle_validation.yml/badge.svg)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/VISUS-Health-IT-GmbH/NightlyTestsPlugin/actions/workflows/gradle.yml)

Gradle Plugin to configure which (j)Unit tests should only be run nightly and not everytime a CI/CD pipeline is triggered.

## Usage

To find out how to apply this plugin to your Gradle project see the information over at the
[Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.visus.infrastructure.nightlytests)!

## Configuration

One necessary parameter needed otherwise the use of the plugin is useless and instantly fails.
The parameter must be set in the projects own gradle.properties file.

```properties
# Which test classes should me handled only when running a nightly build
# list is separated using ","
plugins.nightlytests.listOfTests=List<String>
```

To enable this exclusion of tests only run nightly you must add a system property to the Gradle
task call:

```shell
gradlew test -DBUILDSERVER=NIGHTLYBUILD
```

The value of the environment doesn't matter while it contains anything with *nightly* (case-insensitive)!
