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

The GW Maven Plugin provides function tools for discovering Java classes and reading structured class metadata from the Maven project classpath. These tools are intended for AI-assisted workflows that need to inspect project or dependency classes without manually searching source and jar files.

## Available Function Tools

### `find_class`

Finds fully qualified Java class names whose simple class names match a regular expression.

#### What it does

This tool searches the classes visible from the current Maven project context and returns matching fully qualified class names as a comma-separated list.

It is useful when you know part of a class name, or want to locate candidate classes before requesting full details with `get_class_info`.

#### Features

- Searches using the class simple name, not the full package name.
- Supports regular expression matching.
- Works across classes available from the scanned project classpath.
- Returns a compact result that can be used as input for follow-up inspection.

#### Input parameters

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `className` | `string` | Yes | Regular expression pattern used to match class simple names. |

#### Output

Returns a comma-separated list of fully qualified class names.

If no class matches, the tool returns:

```text
Class not found.
```

#### Typical use cases

- Find all classes whose names match `.*Service`
- Search for a known type when the package name is unknown
- Discover candidate classes before using `get_class_info`

#### Example

Input:

```json
{
  "className": ".*Service"
}
```

---

### `get_class_info`

Returns detailed reflective metadata for a specific fully qualified Java class name.

#### What it does

This tool loads a class from the current Maven project classpath and returns structured JSON describing the class.

The response can include:

- Class name
- Modifiers
- Superclass
- Implemented interfaces
- Non-private fields
- Constructors
- Non-private methods
- Declared annotations
- Classpath location
- Maven artifact coordinates for dependency classes
- Source file path when available in the project sources

#### Features

- Provides structured JSON output for reliable downstream use.
- Includes both type hierarchy and member information.
- Exposes origin details such as jar or output path.
- Identifies dependency artifact coordinates when the class comes from a resolved artifact.
- Resolves project source file paths for classes available in compile source roots.

#### Input parameters

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `className` | `string` | Yes | Fully qualified Java class name to inspect. |

#### Output

Returns a JSON object with class metadata. Common properties include:

| Property | Description |
| --- | --- |
| `className` | Fully qualified class name |
| `modifiers` | Java modifiers for the class |
| `superclass` | Fully qualified superclass name, when present |
| `interfaces` | Array of implemented interface names |
| `fields` | Array of non-private declared fields |
| `constructors` | Array of declared constructors |
| `methods` | Array of non-private declared methods |
| `annotations` | Array of declared annotation strings |
| `path` | Directory or jar path where the class was resolved |
| `artifact` | Maven coordinates in `groupId:artifactId:version` form, when the class comes from a dependency |
| `sourcePath` | Source file path, when a matching project source file exists |

If the class cannot be found, the tool returns an error object such as:

```json
{
  "error": "Class not found: com.example.MissingType"
}
```

#### Typical use cases

- Inspect available methods on a known class
- Understand inheritance and implemented interfaces
- Determine whether a class comes from project code or a dependency
- Locate the source file for a project class
- Review accessible fields and constructors during analysis

#### Example

Input:

```json
{
  "className": "org.example.MyService"
}
```

## Supporting Components

The function tools are backed by internal helper classes in `org.machanism.machai.gw.maven.tools`.

### `ClassFunctionalTools`

This class registers the available function tools with the AI provider and handles tool execution for class lookup and class inspection.

### `ClassInfoHolder`

This helper builds a class loader from the Maven project classpath, scans available classes, and tracks metadata such as class origin paths, source file locations, and dependency artifact coordinates.

## Notes

- Tool results reflect the scanned project state and may become outdated after code or configuration changes.
- Class discovery uses the Maven project context, so results depend on the current project and its resolved classpath.
- `find_class` matches against simple class names, while `get_class_info` requires a fully qualified class name.
