---
<!-- @guidance: 
Create the `Function Tolls` page:
- Analyze classes in the folder: `src/main/java/org/machanism/machai/gw/maven/tools`.
- Write a general description of the each functional tool.
- Describe a feature and input parameters.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->
canonical: https://machai.machanism.org/gw-maven-plugin/functional-tools.html
---

# Function Tools

The GW Maven Plugin provides function tools that let AI workflows discover Java classes available to the current Maven project and inspect structured metadata for a selected class. These tools operate on a project-aware class loader built from the Maven compile classpath together with the project's main and test output directories.

The implementations live in `src/main/java/org/machanism/machai/gw/maven/tools` and are designed for AI-assisted analysis tasks that need to find classes, inspect APIs, understand inheritance, and identify where a class came from.

## Available Function Tools

### `find_class`

Finds fully qualified Java class names whose simple class names match a regular expression.

#### General description

Use `find_class` when you know all or part of a class name but do not know its package. The tool searches classes visible from the current Maven project and returns matching fully qualified class names.

This tool is especially useful as the first step before calling `get_class_info`.

#### Features

- Searches using the simple class name rather than the full package name
- Supports Java regular expression matching
- Uses the current Maven project context
- Searches classes visible from the compile classpath
- Includes classes from the project's main output and test output directories
- Returns fully qualified class names for follow-up inspection

#### Input parameters

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `className` | `string` | Yes | Regular expression used to match simple class names. |

#### Output

Returns matching fully qualified class names as a comma-separated string.

If no matching class is found, the tool returns:

```text
Class not found.
```

If the current project has not been registered for class scanning, the tool returns:

```text
The function tool don't support this function tool.
```

#### Appropriate use cases

- Find classes such as `.*Service`, `.*Controller`, or `.*Mojo`
- Discover the package name for a known simple class name
- Search the current project context for likely implementation classes
- Gather candidate class names before requesting detailed metadata

#### Example

Input:

```json
{
  "className": ".*Service"
}
```

### `get_class_info`

Returns structured metadata for a specific fully qualified Java class name.

#### General description

Use `get_class_info` when you already know the exact fully qualified class name and want to inspect its accessible structure. The tool loads the class through the Maven-project-aware class loader and returns structured JSON describing the class and selected metadata.

It can inspect both project classes and dependency classes when they are visible from the resolved classpath.

#### Features

- Returns structured JSON output
- Includes the fully qualified class name
- Includes Java modifiers for the class
- Includes the superclass when one exists
- Includes implemented interfaces
- Includes declared non-private fields
- Includes declared constructors
- Includes declared non-private methods
- Includes declared annotations
- Includes the directory or jar path from which the class was resolved
- Includes Maven artifact coordinates for dependency classes when available
- Includes a matching project source file path when available

#### Input parameters

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `className` | `string` | Yes | Fully qualified Java class name to inspect. |

#### Output

Returns a JSON object. Common properties include:

| Property | Description |
| --- | --- |
| `className` | Fully qualified class name |
| `modifiers` | Java modifiers for the class |
| `superclass` | Fully qualified superclass name, when present |
| `interfaces` | List of implemented interface names |
| `fields` | Declared non-private fields with modifier, type, and name |
| `constructors` | Declared constructors with modifiers, name, and parameter types |
| `methods` | Declared non-private methods with modifiers, return type, name, and parameter types |
| `annotations` | Declared annotations on the class |
| `path` | Directory or jar path where the class was resolved |
| `artifact` | Maven coordinates in `groupId:artifactId:version` form when available |
| `sourcePath` | Matching project source file path when available |
| `error` | Error message when the class or project context cannot be resolved |

If the class cannot be found, the tool returns an error object such as:

```json
{
  "error": "Class not found: com.example.MissingType"
}
```

If the current project has not been registered for class scanning, the tool returns an error object such as:

```json
{
  "error": "The function tool don't support this function tool."
}
```

#### Appropriate use cases

- Inspect the accessible API surface of a known class
- Review constructors, methods, fields, and annotations during analysis
- Understand inheritance and implemented interfaces
- Determine whether a class comes from project code or a dependency
- Locate the source file for a project class when it exists in compile source roots

#### Example

Input:

```json
{
  "className": "org.example.MyService"
}
```

## Supporting Classes

### `ClassFunctionalTools`

`ClassFunctionalTools` registers the `find_class` and `get_class_info` function tools with the AI provider. It keeps a project-based cache of `ClassInfoHolder` instances so tool calls can be resolved against the current Maven project base directory. It also extracts the `className` input parameter, coordinates tool execution, and formats the structured response returned by `get_class_info`.

### `ClassInfoHolder`

`ClassInfoHolder` manages class discovery and metadata lookup for a single Maven project. It builds a dedicated `URLClassLoader` from the compile classpath and output directories, scans visible classes, maps classes to their origin path, records dependency artifact coordinates, and resolves matching project source files when possible.

Its internal scanning behavior includes the following:

- Captures classes visible from the project-aware class loader
- Searches by regular expression against simple class names
- Loads classes reflectively by fully qualified name
- Records the directory or jar file where a class was found
- Associates dependency classes with Maven coordinates
- Resolves source files from compile source roots
- Records path and artifact metadata only for public or protected loadable classes

## Notes

- Tool results reflect the scanned project state and may become outdated after code or configuration changes.
- `find_class` matches simple class names, not package-qualified names.
- `get_class_info` requires a fully qualified class name.
- Only non-private fields and non-private methods are included in class details.
- Constructors are reported from declared constructors.
- Source file lookup uses the Maven project's compile source roots.
- Path and artifact metadata depend on what can be resolved from the project output directory and dependency artifacts.
