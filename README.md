# StudentCare

Käyttöliittymät 2019, harjoitustyö D

For simple step-by-step instructions, see http://users.utu.fi/jmjmak/help-ideajavafx/

## Requirements

 - OpenJDK / Oracle Java (tested with OpenJDK 8, 10, 11, and 12)
 - SBT (tested with SBT 1.2.8, see https://www.scala-sbt.org/)
 - Git (see https://git-scm.com/downloads/)
 - JavaFX platform files (see https://gluonhq.com/products/javafx/)

The SBT project build file downloads all further requirements:
  - OpenJFX (JavaFX)
  - JUnit 5
  - JQwik
  - other plugins

## Preparation

 - Install Java JDK, not just the JRE.
 - Install SBT and Git.
 - Install Scene Builder for editing fxml files.
 - Install IntelliJ IDEA for IDE support
 - Install IntelliJ IDEA plugins "Git", "Gitlab projects", and "Scala".
 - Download JavaFX platform files. (unless the JDK already includes them)
 - Set the environment variables PATH and JAVAFX_HOME.
 - The environment variables might become effective only after logout/login.
 - 'git clone' this project (or download the zip from the cloud icon above)
 - Import the project from the project directory in the IDE.

## Environment variables

### JAVAFX_HOME (optional)

`JAVAFX_HOME` should point to a directory where the JavaFX SDK files are
located.

1) If the `JAVAFX_HOME` is not set, JavaFX SDK files are downloaded
to `openjfx/` located under the project directory.

2) If the `JAVAFX_HOME` is set, but JavaFX SDK files have not been installed,
the correct JavaFX SDK for the configured JavaFX version will be automatically
downloaded and stored in the directory (make sure SBT has write access to
the directory)

3) If the `JAVAFX_HOME` is set, and the JavaFX SDK files have been installed,
the existing library files will be used for the project.

### PATH

`PATH` should include binaries for Java, Git, and SBT. Obviously, otherwise
the utilities will not work. Test that the following commands can be invoked
from the command line:

* java
* javac
* git
* sbt

At least on Windows the installers may not set up the `PATH` properly by
default. When making changes to `PATH`, sometimes the user needs to log out
and back in before the updated environment variables should take effect.

## Scene Builder

In case you want to manually launch the Scene Builder from command line,
first download the JAR from here:
https://gluonhq.com/products/scene-builder/

Download and install the JavaFX SDK to e.g. ~/openjfx/javafx-sdk-11.0.2:
https://gluonhq.com/products/javafx/

Now, the Scene Builder can be launched with this:

```
java -cp ~/openjfx/scenebuilder-10.0.0-all.jar --module-path ~/openjfx/javafx-sdk-11.0.2/lib --add-modules javafx.controls,javafx.base,javafx.media,javafx.fxml,javafx.graphics,javafx.web,javafx.swing --add-opens javafx.fxml/javafx.fxml=ALL-UNNAMED com.oracle.javafx.scenebuilder.app.SceneBuilderApp
```

In case the GUI shows buggy behavior with drag'n'drop, add `-Djdk.gtk.version=2`
as a Java's command line argument.


## Installation

```
$ git clone https://gitlab.utu.fi/tech/education/gui/studentcare
$ cd studentcare
$ sbt compile
```

## Executing the application

```
$ sbt run
```

## Packaging for easy distribution

First, make sure the build is clean

```
$ sbt clean compile
```

The SBT's assembly plugin can be used to build fat jars that contain all
dependencies and assets:

```
$ sbt assembly
```

The package `target/studentcare-assembly-1.0.jar` will be generated.
This package can be later started simply with:

```
$ java -jar target/studentcare-assembly-1.0.jar
```

## Packaging for distribution

First, make sure the build is clean:

```
$ sbt clean compile
```

The command `package` generates `target/studentcare-1.0.jar`:

```
$ sbt package
```

The command `publishLocal` generates Maven style distribution package
in local Apache Ivy cache:

```
$ sbt publishLocal
```

The command `publish` generates Maven style distribution package
in /tmp:

```
$ sbt publish
```

## Maven export

If you would like to use Eclipse / Netbeans instead of IDEA / SBT, a standard
Maven project description file can be generated with:

```
$ sbt eclipse
$ sbt netbeans
```

The Maven compatible `pom.xml` file is generated on the project root
directory and can be further customized for your IDE. It may not work
without modification (e.g. wrong JDK configuration).

## Document generation

The online documentation for this project is available from:
http://users.utu.fi/jmjmak/help-studentcare/

Make sure you have Doxygen (http://www.doxygen.nl/) installed on the system.
The Javadoc style documentation can be easily generated with:

```
$ doxygen
```

The resulting documentation will be stored in `doc/` under the project
directory. Removing the generated documentation from previous doxygen runs is
recommended before rerunning as some old files may conflict with new ones or
just waste space.
