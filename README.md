[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/beryx-gist/badass-jlink-example-javafx-multiproject/blob/master/LICENSE)
[![Build Status](https://img.shields.io/github/workflow/status/beryx-gist/badass-jlink-example-javafx-multiproject/Gradle%20Build)](https://github.com/beryx-gist/badass-jlink-example-javafx-multiproject/actions?query=workflow%3A%22Gradle+Build%22)

## Badass JLink Plugin Example: JavaFX multi-project ##

A JavaFX "Hello world" application that shows how to use the [Badass JLink Plugin](https://github.com/beryx/badass-jlink-plugin/).

It is structured as a Gradle multi-project containing the following subprojects:

- greeter-api - the greeting service API
- greeter-impl - an implementation of the greeting service API
- gui - a JavaFX application that uses the greeting service   


### Usage
**Running with gradle:**
```
./gradlew run
```

A window containing the text `Hello, OpenJFX!` should appear on the screen.


**Creating and executing a custom runtime image:**
```
./gradlew jlink
cd gui/build/image/bin
./helloFX
```

A window containing the text `Hello, OpenJFX!` should appear on the screen.

**Creating an installable package:**
```
./gradlew jpackage
```

The above command will generate the platform-specific installers in the `gui/build/jpackage` directory.

:bulb: You can check the artifacts produced by the [GitHub actions used to build this project](https://github.com/beryx-gist/badass-jlink-example-javafx-multiproject/actions?query=workflow%3A%22Gradle+Build%22) and download an application package for your platform (such as [from here](https://github.com/beryx-gist/badass-jlink-example-javafx-multiproject/actions/runs/287740188)).
