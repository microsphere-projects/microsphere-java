# Microsphere Java Framework

> Welcome to the Microsphere Java Framework

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/microsphere-projects/microsphere-java)
[![zread](https://img.shields.io/badge/Ask_Zread-_.svg?style=flat&color=00b0aa&labelColor=000000&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQuOTYxNTYgMS42MDAxSDIuMjQxNTZDMS44ODgxIDEuNjAwMSAxLjYwMTU2IDEuODg2NjQgMS42MDE1NiAyLjI0MDFWNC45NjAxQzEuNjAxNTYgNS4zMTM1NiAxLjg4ODEgNS42MDAxIDIuMjQxNTYgNS42MDAxSDQuOTYxNTZDNS4zMTUwMiA1LjYwMDEgNS42MDE1NiA1LjMxMzU2IDUuNjAxNTYgNC45NjAxVjIuMjQwMUM1LjYwMTU2IDEuODg2NjQgNS4zMTUwMiAxLjYwMDEgNC45NjE1NiAxLjYwMDFaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00Ljk2MTU2IDEwLjM5OTlIMi4yNDE1NkMxLjg4ODEgMTAuMzk5OSAxLjYwMTU2IDEwLjY4NjQgMS42MDE1NiAxMS4wMzk5VjEzLjc1OTlDMS42MDE1NiAxNC4xMTM0IDEuODg4MSAxNC4zOTk5IDIuMjQxNTYgMTQuMzk5OUg0Ljk2MTU2QzUuMzE1MDIgMTQuMzk5OSA1LjYwMTU2IDE0LjExMzQgNS42MDE1NiAxMy43NTk5VjExLjAzOTlDNS42MDE1NiAxMC42ODY0IDUuMzE1MDIgMTAuMzk5OSA0Ljk2MTU2IDEwLjM5OTlaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik0xMy43NTg0IDEuNjAwMUgxMS4wMzg0QzEwLjY4NSAxLjYwMDEgMTAuMzk4NCAxLjg4NjY0IDEwLjM5ODQgMi4yNDAxVjQuOTYwMUMxMC4zOTg0IDUuMzEzNTYgMTAuNjg1IDUuNjAwMSAxMS4wMzg0IDUuNjAwMUgxMy43NTg0QzE0LjExMTkgNS42MDAxIDE0LjM5ODQgNS4zMTM1NiAxNC4zOTg0IDQuOTYwMVYyLjI0MDFDMTQuMzk4NCAxLjg4NjY0IDE0LjExMTkgMS42MDAxIDEzLjc1ODQgMS42MDAxWiIgZmlsbD0iI2ZmZiIvPgo8cGF0aCBkPSJNNCAxMkwxMiA0TDQgMTJaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00IDEyTDEyIDQiIHN0cm9rZT0iI2ZmZiIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K&logoColor=ffffff)](https://zread.ai/microsphere-projects/microsphere-java)
[![Maven Build](https://github.com/microsphere-projects/microsphere-java/actions/workflows/maven-build.yml/badge.svg)](https://github.com/microsphere-projects/microsphere-java/actions/workflows/maven-build.yml)
[![Codecov](https://codecov.io/gh/microsphere-projects/microsphere-java/branch/main/graph/badge.svg)](https://app.codecov.io/gh/microsphere-projects/microsphere-java)
![Maven](https://img.shields.io/maven-central/v/io.github.microsphere-projects/microsphere-java.svg)
![License](https://img.shields.io/github/license/microsphere-projects/microsphere-java.svg)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/microsphere-projects/microsphere-java.svg)](http://isitmaintained.com/project/microsphere-projects/microsphere-java "Average time to resolve an issue")
[![Percentage of issues still open](http://isitmaintained.com/badge/open/microsphere-projects/microsphere-java.svg)](http://isitmaintained.com/project/microsphere-projects/microsphere-java "Percentage of issues still open")

## Introduction

Microsphere Java Framework is a foundational library that serves as the backbone
for [MicroSphere](https://github.com/microsphere-projects) ecosystem. It provides a rich set of reusable components,
utilities, and annotation processing capabilities that address common challenges in Java development. Whether you're
building enterprise applications, microservices, or standalone Java tools, this framework offers the building blocks you
need to accelerate development and maintain consistency across your projects.

The framework is designed with modularity at its core, allowing you to use only the components you need while keeping
your application lightweight and efficient. It's built on standard Java APIs and integrates seamlessly with popular
frameworks like Spring, making it a versatile addition to any Java developer's toolkit.

## Features

- Core Utilities
- I/O
- Collection manipulation
- Class loading
- Concurrency
- Reflection
- Networking
- Artifact management
- Event Sourcing
- JMX
- Versioning
- Annotation processing

## Modules

The framework is organized into several key modules:

 Module                           | Purpose                                                                                         
----------------------------------|-------------------------------------------------------------------------------------------------
 microsphere-java-core            | Provides core utilities across various domains like annotations, collections, concurrency, etc. 
 microsphere-annotation-processor | Offers annotation processing capabilities for compile-time code generation                      
 microsphere-java-dependencies    | Manages dependency versions across the project                                                  
 microsphere-java-parent          | Parent POM with shared configurations                                                           

## Getting Started

The easiest way to get started is by adding the Microsphere Java BOM (Bill of Materials) to your project's pom.xml:

```xml

<dependencyManagement>
    <dependencies>
        ...
        <!-- Microsphere Dependencies -->
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-java-dependencies</artifactId>
            <version>${microsphere-java.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        ...
    </dependencies>
</dependencyManagement>
```

Then add the specific modules you need:

```xml

<dependencies>
    <!-- Core utilities -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-java-core</artifactId>
    </dependency>

    <!-- Annotation processing (optional) -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-annotation-processor</artifactId>
    </dependency>
</dependencies>
```

### Quick Examples

```java
import io.microsphere.util.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MicrosphereTest {

    @Test
    public void testStringUtils() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertFalse(StringUtils.isBlank("Hello"));
    }
}
```

## Building from Source

You don't need to build from source unless you want to try out the latest code or contribute to the project.

To build the project, follow these steps:

1. Clone the repository:

```bash
git clone https://github.com/microsphere-projects/microsphere-java.git
```

2. Build the source:

- Linux/MacOS:

```bash
./mvnw package
```

- Windows:

```powershell
mvnw.cmd package
```

## Contributing

We welcome your contributions! Please read [Code of Conduct](./CODE_OF_CONDUCT.md) before submitting a pull request.

## Reporting Issues

* Before you log a bug, please search the [issues](./issues) to see if someone has already reported the problem.
* If the issue doesn't already exist, [create a new issue]({./issues/new).
* Please provide as much information as possible with the issue report.

## Documents

The Microsphere Java Framework maintains:

- [Reference](https://microsphere-projects.github.io/microsphere-java/) (TODO)
- [Wiki](https://github.com/microsphere-projects/microsphere-java/wiki)
- JavaDoc
    - [microsphere-java-core](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-java-core)
    - [microsphere-annotation-processor](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-annotation-processor)
- User Guides
    - [DeepWiki](https://deepwiki.com/microsphere-projects/microsphere-java)
    - [Zread](https://zread.ai/microsphere-projects/microsphere-java)
      
## License

The Microsphere Java is released under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)