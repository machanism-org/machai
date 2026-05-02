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

The GW Maven Plugin provides function tools for exploring Java classes that are visible from the current Maven project. These tools support AI-assisted workflows that need to discover classes by name and inspect reflective metadata such as members, inheritance, annotations, source locations, and dependency origins.

The tools described on this page are implemented in `src/main/java/org/machanism/machai/gw/maven/tools` and operate on class information collected from the project compile classpath together with the project output and test output directories.

## Available Function Tools

### `find_class`

Finds fully qualified Java class names whose simple class names match a regular expression.

#### General description

Use this tool when you know a class name pattern but do not know the package name. It searches the classes visible to the current Maven project and returns matching fully qualified class names.

This is typically the best starting point before calling `get_class_info`.

#### Features

- Matches against the simple class name, not the package name
- Accepts regular expression patterns
- Searches classes visible from the current project class loader
- Includes classes available from the compile classpath and project outputs
- Returns matches in a compact format for quick follow-up use

#### Input parameters

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `className` | `string` | Yes | Regular expression pattern used to match class simple names. |

#### Output

Returns matching fully qualified class names as a comma-separated list.

If no class matches, the tool returns:

```text
Class not found.
```

If the current project has not been scanned and registered for tool support, the tool returns:

```text
The function tool don't support this function tool.
```

#### Appropriate use cases

- Find classes such as `.*Service`, `.*Controller`, or `.*Mojo`
- Discover the package of a known type name
- Collect candidate class names before requesting detailed metadata
- Search for implementation types available from the current project context

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

Use this tool when you already know the exact fully qualified class name and want to inspect the structure of that class. It loads the class from the current Maven project context and returns AI-friendly JSON describing the type and selected members.

The tool can be used for both project classes and dependency classes that are visible from the resolved classpath.

#### Features

- Returns structured JSON output
- Includes the fully qualified class name and Java modifiers
- Includes the superclass when one exists
- Includes implemented interfaces
- Includes non-private declared fields
- Includes declared constructors
- Includes non-private declared methods
- Includes declared annotations
- Includes the directory or jar path where the class was resolved
- Includes Maven artifact coordinates when the class comes from a dependency
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
| `interfaces` | Implemented interface names |
| `fields` | Non-private declared fields with modifier, type, and name |
| `constructors` | Declared constructors with modifiers, name, and parameter types |
| `methods` | Non-private declared methods with modifiers, return type, name, and parameter types |
| `annotations` | Declared annotations on the class |
| `path` | Directory or jar path where the class was resolved |
| `artifact` | Maven coordinates in `groupId:artifactId:version` form when the class comes from a dependency |
| `sourcePath` | Matching project source file path when available |

If the class cannot be found, the tool returns an error object such as:

```json
{
  "error": "Class not found: com.example.MissingType"
}
```

If the current project has not been scanned and registered for tool support, the tool returns an error object such as:

```json
{
  "error": "The function tool don't support this function tool."
}
```

#### Appropriate use cases

- Inspect the accessible structure of a known class
- Review constructors, methods, and fields during analysis tasks
- Understand inheritance and implemented interfaces
- Determine whether a class comes from project code or a dependency
- Locate the matching source file for a project class when available

#### Example

Input:

```json
{
  "className": "org.example.MyService"
}
```

## Supporting Classes

### `ClassFunctionalTools`

Registers the `find_class` and `get_class_info` tools with the AI provider and routes tool execution by current Maven project base directory. It also formats the structured class details returned by `get_class_info`.

### `ClassInfoHolder`

Builds a dedicated class loader from the Maven project classpath, test output directory, and main output directory. It scans visible classes, supports regular-expression discovery, loads classes for reflection, maps classes to their origin path, resolves dependency artifact coordinates, and attempts to locate matching project source files.

## Notes

- Tool results reflect the scanned project state and may become outdated after code or configuration changes.
- `find_class` matches simple class names rather than full package names.
- `get_class_info` requires a fully qualified class name.
- Only non-private fields and non-private methods are included in class details.
- Source file lookup is based on the project's compile source roots.
- Class origin metadata depends on what is available from the current Maven project classpath and resolved artifacts.
