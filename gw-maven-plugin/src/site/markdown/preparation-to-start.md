---
canonical: https://machai.machanism.org/gw-maven-plugin/preparation-to-start.html
---

# Preparation to Start

## Prerequisites

Before you begin, ensure you have the following:

- **Java Development Kit (JDK):**  
  Java 11 or higher (as specified in your project’s `pom.xml`).
- **Maven:**  
  Version 3.6.0 or higher is recommended.
- **Internet Access:**  
  Required for downloading dependencies and, if using GenAI features, for API access.
- **GW Maven Plugin Artifact:**  
  The plugin should be available in your Maven repository or as a direct JAR download.

## Project Structure

Your project should follow a standard Maven layout:

```
project-root/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       ├── java/
│       └── resources/
└── ...
```

If you use a multi-module Maven project, ensure all modules are properly defined in the parent `pom.xml`.

## Configuration

### Add the Plugin to Your `pom.xml`

Add the GW Maven Plugin to your build plugins section:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.machanism.machai</groupId>
      <artifactId>gw-maven-plugin</artifactId>
      <version>0.0.9-SNAPSHOT</version>
    </plugin>
  </plugins>
</build>
```

### Configure Required Properties

- **GenAI Credentials:**  
  If using GenAI features, set the following environment variables or system properties:
  - `GENAI_USERNAME`
  - `GENAI_PASSWORD`

- **Guidance and Instructions:**  
  Prepare your default guidance and additional instructions files (e.g., `guidance.txt`, `instructions.txt`).

## Example Command

To run the GW Maven Plugin with default guidance and instructions:

```sh
mvn org.machanism.machai:gw-maven-plugin:0.0.10:std \
  -Dgw.guidance=file:guidance.txt \
  -Dgw.instructions=file:instructions.txt \
  -DGENAI_USERNAME=your-username \
  -DGENAI_PASSWORD=your-password
```