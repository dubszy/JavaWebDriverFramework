# PODriverFramework
Wrapper framework for Selenium WebDriver using the Page Object Model.
Information about the Page Object Model can be found
[here](https://github.com/SeleniumHQ/selenium/wiki/PageObjects)

## Building

### Overview
This framework is built using [Gradle](https://gradle.org/). Build scripts for
Gradle are written in [Groovy](http://groovy-lang.org/). For more information
on both of these, see the
[Gradle docs](https://docs.gradle.org/current/userguide/userguide.html) and the
[Groovy docs](http://groovy-lang.org/documentation.html).

### Build (without tests)
Run the following from the command line to build this project without invoking
tests:
```
./gradlew
```

### Running Tests
Run the following from the command line to invoke the 'test' task in
build.gradle. This will execute all tests in the 'src/test' folder:
```
./gradlew test
```

## Project Structure

### gradle/
The Gradle wrapper and settings.

### src/main/
Holds all the source code for the framework. Contains no tests.

### src/test/
Holds all tests. Note that this includes tests of the framework (unit, lint
tests, etc...) as well as tests designed to be run against other source code
(i.e, automation tests)

## Test Writing
Tests utilizing this framework should extend BaseTest
(src/test/java/com.mwaltman.test.common.BaseTest). See the BaseTest class for
more documentation. An example test can be found in
src/test/java/com.mwaltman.test.sandbox.SandboxTest.
