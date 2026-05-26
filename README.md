# Microsphere Java Framework

> Welcome to the Microsphere Java Framework

[![DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/microsphere-projects/microsphere-java)
[![Maven Build](https://github.com/microsphere-projects/microsphere-java/actions/workflows/maven-build.yml/badge.svg)](https://github.com/microsphere-projects/microsphere-java/actions/workflows/maven-build.yml)
[![Codecov](https://codecov.io/gh/microsphere-projects/microsphere-java/branch/main/graph/badge.svg)](https://app.codecov.io/gh/microsphere-projects/microsphere-java)
![Maven](https://img.shields.io/maven-central/v/io.github.microsphere-projects/microsphere-java.svg)
![License](https://img.shields.io/github/license/microsphere-projects/microsphere-java.svg)

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Modules](#modules)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Maven](#maven)
- [Gradle](#gradle)
- [Usage Examples](#usage-examples)
- [Building from Source](#building-from-source)
- [Documentation](#documentation)
- [Contributing](#contributing)
- [Getting Help](#getting-help)
- [Maintainers](#maintainers)
- [License](#license)

## Introduction

Microsphere Java Framework is a foundational library that serves as the backbone
for the [MicroSphere](https://github.com/microsphere-projects) ecosystem. It provides a rich set of reusable components,
utilities, and annotation processing capabilities that address common challenges in Java development. Whether you're
building enterprise applications, microservices, or standalone Java tools, this framework offers the building blocks you
need to accelerate development and maintain consistency across your projects.

The framework is designed with modularity at its core, allowing you to use only the components you need while keeping
your application lightweight and efficient. It's built on standard Java APIs and integrates seamlessly with popular
frameworks like Spring, making it a versatile addition to any Java developer's toolkit.

## Features

- **String & Collection Utilities** — Null-safe helpers for strings, arrays, lists, sets, maps, and deques
- **Reflection Utilities** — Simplified access to fields, methods, constructors, and generic type arguments
- **Type Conversion** — Extensible `Converter` SPI with built-in converters for standard Java types (primitives, collections, `Duration`, `InputStream`, and more)
- **Event Dispatching** — Lightweight `EventDispatcher` with sequential and parallel execution modes
- **Class Loading & Artifact Detection** — Utilities for classpath scanning, JAR introspection, and Maven/module artifact resolution
- **Networking** — Custom `URLStreamHandler` and URL utility helpers
- **I/O** — Enhanced file, stream, and charset utilities, plus a file-watch service
- **Concurrency** — Delegating executor wrappers and custom thread factory
- **Configuration Properties** — SPI-based property loading with annotation-driven generation
- **Annotation Processing** — Compile-time processor for `@ConfigurationProperty` metadata generation
- **Language Model** — Components and utilities for the Java Language Model API
- **JDK Tools** — Helpers bridging the Java compiler and annotation processing tool APIs
- **Testing Support** — Base classes and utilities for JUnit 5-based unit tests

## Modules

The framework is organized into several focused modules:

| Module                           | Artifact ID                        | Purpose                                                                                              |
|----------------------------------|------------------------------------|------------------------------------------------------------------------------------------------------|
| microsphere-java-annotations     | `microsphere-java-annotations`     | Common annotations (`@Nullable`, `@Nonnull`, `@Immutable`, `@Experimental`, `@Since`, …)             |
| microsphere-java-core            | `microsphere-java-core`            | Core utilities: strings, collections, reflection, I/O, concurrency, events, type conversion, and more |
| microsphere-lang-model           | `microsphere-lang-model`           | Components and utilities for the Java Language Model API                                             |
| microsphere-jdk-tools            | `microsphere-jdk-tools`            | Helpers for Java compiler and JDK tool APIs                                                          |
| microsphere-annotation-processor | `microsphere-annotation-processor` | Compile-time annotation processor for `@ConfigurationProperty` metadata generation                  |
| microsphere-java-test            | `microsphere-java-test`            | Models and utilities for JUnit 5-based testing                                                       |
| microsphere-java-dependencies    | `microsphere-java-dependencies`    | BOM (Bill of Materials) managing dependency versions across the project                              |
| microsphere-java-parent          | `microsphere-java-parent`          | Parent POM with shared build configuration                                                           |

## Prerequisites

- **Java** 8, 11, 17, 21, or 25 (all actively tested in CI)
- **Maven** 3.6+ (or use the included `mvnw`/`mvnw.cmd` wrapper)

## Getting Started

### Maven

Add the BOM to your `pom.xml` to manage versions centrally:

```xml
<dependencyManagement>
  <dependencies>
      <dependency>
          <groupId>io.github.microsphere-projects</groupId>
          <artifactId>microsphere-java-dependencies</artifactId>
          <version>${microsphere-java.version}</version>
          <type>pom</type>
          <scope>import</scope>
      </dependency>
  </dependencies>
</dependencyManagement>
```

Then declare only the modules you need:

```xml
<dependencies>
  <!-- Core utilities -->
  <dependency>
      <groupId>io.github.microsphere-projects</groupId>
      <artifactId>microsphere-java-core</artifactId>
  </dependency>

  <!-- Compile-time annotation processor (optional) -->
  <dependency>
      <groupId>io.github.microsphere-projects</groupId>
      <artifactId>microsphere-annotation-processor</artifactId>
      <optional>true</optional>
  </dependency>
</dependencies>
```

### Gradle

```groovy
dependencies {
  implementation platform("io.github.microsphere-projects:microsphere-java-dependencies:${microsphereJavaVersion}")

  implementation "io.github.microsphere-projects:microsphere-java-core"
  annotationProcessor "io.github.microsphere-projects:microsphere-annotation-processor"
}
```

### Usage Examples

#### String Utilities

```java
import io.microsphere.util.StringUtils;

StringUtils.isBlank(null);      // true
StringUtils.isBlank("");        // true
StringUtils.isBlank("  ");      // true
StringUtils.isBlank("Hello");   // false
```

#### Collection Utilities

```java
import io.microsphere.collection.CollectionUtils;

CollectionUtils.isEmpty(null);              // true
CollectionUtils.isEmpty(List.of());         // true
CollectionUtils.isEmpty(List.of("item"));   // false
```

#### Reflection Utilities

```java
import io.microsphere.reflect.FieldUtils;
import io.microsphere.reflect.MethodUtils;

// Read a private field value without boilerplate
Object value = FieldUtils.getFieldValue(myObject, "fieldName");

// Find all declared methods matching a predicate
Set<Method> methods = MethodUtils.findMethods(MyClass.class, m -> m.isAnnotationPresent(Override.class));
```

#### Type Conversion

```java
import io.microsphere.convert.Converters;

// Convert a String to Integer
Integer number = Converters.convert("42", Integer.class);

// Convert a String to a List of Strings
List<String> items = Converters.convert("a,b,c", List.class);
```

#### Event Dispatching

```java
import io.microsphere.event.EventDispatcher;
import io.microsphere.event.EventListener;

// Sequential (direct) dispatcher
EventDispatcher dispatcher = EventDispatcher.newDefault();
dispatcher.addEventListener((EventListener<MyEvent>) event -> System.out.println("Received: " + event));
dispatcher.dispatch(new MyEvent("hello"));

// Parallel dispatcher with a custom thread pool
Executor executor = Executors.newFixedThreadPool(4);
EventDispatcher parallel = EventDispatcher.parallel(executor);
parallel.addEventListener(myListener);
parallel.dispatch(new MyEvent("world"));
```

#### Artifact / Version Detection

```java
import io.microsphere.util.Version;

// Detect the runtime version of a library from its JAR manifest
Version springVersion = Version.ofVersion(org.springframework.core.SpringVersion.class);
boolean isModern = springVersion.isGreaterThanOrEqualTo("6.0.0");
```

## Building from Source

You don't need to build from source to use the library — published artifacts are available on Maven Central.
Clone and build only if you want to try the latest unreleased code or contribute to the project.

```bash
git clone https://github.com/microsphere-projects/microsphere-java.git
cd microsphere-java
```

**Linux / macOS**

```bash
./mvnw package
```

**Windows**

```powershell
mvnw.cmd package
```

To run the full test suite with coverage:

```bash
./mvnw test --activate-profiles test,coverage
```

## Documentation

| Resource | Link |
|----------|------|
| User Guide | [user-guide.md](./user-guide.md) |
| AI-powered docs (DeepWiki) | [![DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/microsphere-projects/microsphere-java) |
| Ask Zread | [![zread](https://img.shields.io/badge/Ask_Zread-_.svg?style=flat&color=00b0aa&labelColor=000000&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQuOTYxNTYgMS42MDAxSDIuMjQxNTZDMS44ODgxIDEuNjAwMSAxLjYwMTU2IDEuODg2NjQgMS42MDE1NiAyLjI0MDFWNC45NjAxQzEuNjAxNTYgNS4zMTM1NiAxLjg4ODEgNS42MDAxIDIuMjQxNTYgNS42MDAxSDQuOTYxNTZDNS4zMTUwMiA1LjYwMDEgNS42MDE1NiA1LjMxMzU2IDUuNjAxNTYgNC45NjAxVjIuMjQwMUM1LjYwMTU2IDEuODg2NjQgNS4zMTUwMiAxLjYwMDEgNC45NjE1NiAxLjYwMDFaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00Ljk2MTU2IDEwLjM5OTlIMi4yNDE1NkMxLjg4ODEgMTAuMzk5OSAxLjYwMTU2IDEwLjY4NjQgMS42MDE1NiAxMS4wMzk5VjEzLjc1OTlDMS42MDE1NiAxNC4xMTM0IDEuODg4MSAxNC4zOTk5IDIuMjQxNTYgMTQuMzk5OUg0Ljk2MTU2QzUuMzE1MDIgMTQuMzk5OSA1LjYwMTU2IDE0LjExMzQgNS42MDE1NiAxMy43NTk5VjExLjAzOTlDNS42MDE1NiAxMC42ODY0IDUuMzE1MDIgMTAuMzk5OSA0Ljk2MTU2IDEwLjM5OTlaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik0xMy43NTg0IDEuNjAwMUgxMS4wMzg0QzEwLjY4NSAxLjYwMDEgMTAuMzk4NCAxLjg4NjY0IDEwLjM5ODQgMi4yNDAxVjQuOTYwMUMxMC4zOTg0IDUuMzEzNTYgMTAuNjg1IDUuNjAwMSAxMS4wMzg0IDUuNjAwMUgxMy43NTg0QzE0LjExMTkgNS42MDAxIDE0LjM5ODQgNS4zMTM1NiAxNC4zOTg0IDQuOTYwMVYyLjI0MDFDMTQuMzk4NCAxLjg4NjY0IDE0LjExMTkgMS42MDAxIDEzLjc1ODQgMS42MDAxWiIgZmlsbD0iI2ZmZiIvPgo8cGF0aCBkPSJNNCAxMkwxMiA0TDQgMTJaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00IDEyTDEyIDQiIHN0cm9rZT0iI2ZmZiIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K&logoColor=ffffff)](https://zread.ai/microsphere-projects/microsphere-java) |
| Wiki | [GitHub Wiki](https://github.com/microsphere-projects/microsphere-java/wiki) |
| Release Notes | [release-notes.md](./release-notes.md) |

### JavaDoc

- [microsphere-java-annotations](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-java-annotations)
- [microsphere-java-core](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-java-core)
- [microsphere-lang-model](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-lang-model)
- [microsphere-jdk-tools](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-jdk-tools)
- [microsphere-annotation-processor](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-annotation-processor)
- [microsphere-java-test](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-java-test)

## Contributing

Contributions are welcome! To get started:

1. Fork the repository and create a feature branch.
2. Make your changes with tests.
3. Ensure all tests pass: `./mvnw test`
4. Submit a pull request against the `main` branch.

Please read [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) before contributing.

## Getting Help

- **Bug reports & feature requests** — [Open an issue](https://github.com/microsphere-projects/microsphere-java/issues/new) (search [existing issues](https://github.com/microsphere-projects/microsphere-java/issues) first)
- **Questions & discussions** — [GitHub Discussions](https://github.com/microsphere-projects/microsphere-java/discussions)
- **AI-powered documentation** — [DeepWiki](https://deepwiki.com/microsphere-projects/microsphere-java)

## Maintainers

| Name | Role | Contact |
|------|------|---------|
| [Mercy Ma (mercyblitz)](https://github.com/mercyblitz) | Lead Architect & Developer | mercyblitz@gmail.com |

## License

The Microsphere Java Framework is released under the [Apache License 2.0](./LICENSE).
