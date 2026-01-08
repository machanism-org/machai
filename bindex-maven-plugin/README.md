# bindex-maven-plugin

## Overview

**bindex-maven-plugin** is a multi-module Java project designed to streamline and extend Maven plugin functionality for OSGi bundle indexing. The project provides tools and modules to automate the generation of OSGi metadata, support advanced configuration, and facilitate integration into Maven builds. Key features include modular structure, easy extensibility, and adherence to the latest OSGi and Maven conventions.

## Usage

To build and use the project, clone the repository and run the following commands from the project root:

~~~shell
# Clone the repository
$ git clone <repository-url>
$ cd bindex-maven-plugin

# Build all modules
$ mvn clean install

# Use the plugin in your Maven project
<plugin>
    <groupId>org.osgi</groupId>
    <artifactId>bindex-maven-plugin</artifactId>
    <version>YOUR_VERSION_HERE</version>
    <executions>
        <execution>
            <goals>
                <goal>index</goal>
            </goals>
        </execution>
    </executions>
</plugin>
~~~

You can customize plugin behavior through Maven configuration properties. Refer to each module's documentation for advanced options and specifics.

## Reference

This project is a child of the parent Maven project defined in the repository. For details and extended documentation, see the [parent project documentation](../README.md).