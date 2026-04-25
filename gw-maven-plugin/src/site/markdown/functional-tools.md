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

The GW Maven Plugin includes function tools that help AI-driven workflows inspect Java types available from the current Maven project. These tools can search for candidate classes by simple name and return structured reflective metadata for a specific class.

The tools in this page are implemented by the classes in `src/main/java/org/machanism/machai/gw/maven/tools` and operate against class information collected from the Maven project classpath.

## Available Function Tools

### `find_class`

Finds fully qualified Java class names whose simple class names match a regular expression.

#### General description

Use this tool when you know all or part of a class name but do not know its package. It searches the classes visible to the current Maven project and returns matching fully qualified class names.

This makes it a good first step before calling `get_class_info`.

#### Features

- Matches against the simple class name rather than the full package name
- Supports regular expression patterns
- Searches classes visible from the current Maven project context
- Returns a compact list of matches that can be used in follow-up inspection

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

If the current project has not been registered for tool support, the tool returns:

```text
The function tool don't support this function tool.
```

#### Appropriate use cases

- Find classes such as `.*Service` or `.*Controller`
- Discover the package of a known type name
- Collect candidate class names before requesting detailed metadata

#### Example

Input:

```json
{
  "className": ".*Service"
}
```

---

### `get_class_info`

Returns structured metadata for a specific fully qualified Java class name.

#### General description

Use this tool when you already know the exact fully qualified class name and want details about the class structure. It loads the class from the current Maven project context and produces JSON describing the type and selected members.

The response is designed for AI-friendly consumption and helps with analysis of both project classes and dependency classes.

#### Features

- Returns structured JSON output
- Includes class name and Java modifiers
- Includes superclass and implemented interfaces
- Includes non-private declared fields
- Includes declared constructors
- Includes non-private declared methods
- Includes declared annotations
- Includes origin metadata such as classpath location
- Includes dependency coordinates when the class comes from a Maven artifact
- Includes source file path when a matching project source file is available

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
| `constructors` | Declared constructors with modifiers and parameter types |
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

If the current project has not been registered for tool support, the tool returns an error object such as:

```json
{
  "error": "The function tool don't support this function tool."
}
```

#### Appropriate use cases

- Inspect the accessible structure of a known class
- Review constructors, methods, and fields for analysis tasks
- Understand inheritance and implemented interfaces
- Check whether a class comes from project code or a dependency
- Locate the matching project source file when available

#### Example

Input:

```json
{
  "className": "org.example.MyService"
}
```

## Supporting Classes

### `ClassFunctionalTools`

This class registers the function tools with the AI provider and handles execution of `find_class` and `get_class_info` for the current Maven project context.

### `ClassInfoHolder`

This class builds and uses a class loader based on the Maven project classpath. It scans available classes, supports regular-expression class discovery, loads classes for reflection, and tracks related metadata such as class origin path, source path, and Maven artifact coordinates.

## Notes

- Tool results reflect the scanned project state and may become outdated after code or configuration changes.
- `find_class` matches simple class names, not package names.
- `get_class_info` requires a fully qualified class name.
- Metadata availability depends on the current Maven project classpath and source roots.
