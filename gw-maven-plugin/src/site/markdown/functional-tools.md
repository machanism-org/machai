<!-- @guidance: 
Create the `Function Tolls` page:
- Analyze classes in the folder: `src/main/java/org/machanism/machai/gw/maven/tools`.
- Write a general description of the each functional tool.
- Describe a feature and input parameters.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->

# Function Tools

The GW Maven Plugin exposes function tools for discovering Java classes in the current Maven project and for inspecting the reflective structure of a selected class. `ClassFunctionalTools` registers the tools, while `ClassInfoHolder` builds and searches the project-aware classpath used by them.

The tools are intended for AI-assisted workflows that need to discover implementation classes, inspect an API, or determine whether a class comes from project output or a Maven dependency. Results are based on the project state when it is scanned and can become stale after source or build configuration changes.

## `find-class`

### Purpose

Finds fully qualified Java class names whose **simple (short) names** match a Java regular expression. Use this tool when you know a class-name pattern but do not know the package, such as when looking for service, controller, or Maven Mojo implementations. It is commonly the discovery step before `get-class-info`.

### Features

- Matches the regular expression against `ClassInfo.getSimpleName()`, not the package-qualified name.
- Searches classes visible through the Maven project's compile classpath, test output directory, and main output directory.
- Returns fully qualified names that can be passed to `get-class-info`.
- Rejects searches that produce more than 10 matches so that callers refine an overly broad pattern.
- Uses a cached scan for the registered project; later source or configuration changes are not automatically reflected.

### Input parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `class-name` | `string` | Yes | Java regular expression matched against each class's simple name. Matching uses the regular-expression `matches` behavior, so the pattern must match the complete simple name. |
| `project-dir` | `File` | Yes | Base directory of the registered Maven project to search. |

### Result and errors

On success, the result is a list of fully qualified class-name strings. If there are no matches, the tool raises `IllegalArgumentException` with `Class not found.`. If more than 10 classes match, it raises `IllegalArgumentException` and reports the count and the maximum; narrow the pattern and try again. If `project-dir` has not been registered, it raises `IllegalArgumentException` with `The project does not have classInfoProjectMap.`. An invalid regular expression may also fail with the regular-expression compilation error.

### Example

```json
{
  "class-name": ".*Service",
  "project-dir": "C:/work/example"
}
```

This searches for classes whose simple names end in `Service`, for example `com.example.user.UserService`.

## `get-class-info`

### Purpose

Returns reflective metadata for a Java class identified by its fully qualified name. Use it after discovering a class, or when you already know the exact class name and need to understand its accessible API, inheritance, annotations, or origin.

### Features

- Loads the class with the registered Maven project's class loader.
- Reports the class name, modifiers, superclass (when present), and directly implemented interfaces.
- Reports declared non-private fields and methods, including modifiers, types, names, and parameter types.
- Reports all declared constructors, including private constructors, with modifiers, name, and parameter types.
- Reports declared class annotations as strings.
- Reports the class's resolved directory or JAR path, dependency coordinates when available, and a matching source path when the class belongs to a compile source root.
- Uses the same cached project scan as `find-class`, so the metadata reflects the scanned project state.

### Input parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `class-name` | `string` | Yes | Fully qualified Java class name, such as `com.example.user.UserService`. |
| `project-dir` | `File` | Yes | Base directory of the registered Maven project used to load and inspect the class. |

### Result

On success, the tool returns a JSON-serializable object containing the following properties when applicable:

| Property | Description |
| --- | --- |
| `className` | Fully qualified name returned by the loaded `Class`. |
| `modifiers` | Java class modifiers formatted as text. |
| `superclass` | Fully qualified direct superclass name; omitted when `Class.getSuperclass()` is `null`. |
| `interfaces` | Names of interfaces directly implemented by the class. |
| `fields` | Declared non-private fields, each with `modifiers`, `type`, and `name`. |
| `constructors` | All declared constructors, each with `modifiers`, `name`, and `parameterTypes`. |
| `methods` | Declared non-private methods, each with `modifiers`, `returnType`, `name`, and `parameterTypes`. |
| `annotations` | String representations of the class's declared annotations. |
| `path` | Directory or JAR path recorded for the class, when it was found during location scanning. |
| `artifact` | Dependency coordinates in `groupId:artifactId:version` form, when available. |
| `sourcePath` | Matching `.java` file under a Maven compile source root, when available. |

A missing class causes `ClassNotFoundException`. An unregistered `project-dir` causes `IllegalArgumentException` with `The project does not have classInfoProjectMap.`.

### Example

```json
{
  "class-name": "org.example.MyService",
  "project-dir": "C:/work/example"
}
```

## Supporting implementation classes

### `ClassFunctionalTools`

`ClassFunctionalTools` implements `FunctionTools` and exposes the two callable operations. It maintains a map from Maven project base directories to `ClassInfoHolder` instances. A project can be registered through the constructor or `scanProjectClasses(MavenProject)`. Each tool call uses `project-dir` to select the corresponding holder, extracts the requested class-name value, and either returns the result or reports an error.

### `ClassInfoHolder`

`ClassInfoHolder` owns discovery for one Maven project. Its class discovery scan is lazy: the class loader and class list are initialized on the first class search, class load, or class-origin lookup. The loader is built from Maven compile-classpath elements plus the test and main output directories. Guava `ClassPath` then supplies the visible class list used by `find-class`. Source lookup is separate and checks the project's compile source roots while generating the `sourcePath` result property.

For origin metadata, the holder scans the main output directory and resolved Maven dependency artifacts. It records loadable public and protected classes, their directory or JAR path, and dependency coordinates. It searches compile source roots for source files and removes a nested-class suffix (for example, `$Inner`) before looking for the top-level `.java` file. Missing class paths are skipped and class entries that cannot be loaded are logged and ignored.

## Choosing the right tool

1. Call `find-class` when the package or exact class name is unknown.
2. Refine the regular expression if the search returns more than 10 matches.
3. Call `get-class-info` with one of the returned fully qualified names to inspect its structure and origin.
4. Provide the registered Maven project base directory as `project-dir` for both calls.

## Important limitations

- Both tools require a project to have been registered with `ClassFunctionalTools`.
- `find-class` searches the cached classpath scan and matches only simple names.
- `get-class-info` requires a fully qualified name and uses Java class loading; unavailable or unloadable classes cannot be inspected.
- Fields and methods are filtered to exclude private members; constructors are not filtered.
- Interfaces listed are direct interfaces, and the superclass listed is the direct superclass rather than the complete hierarchy.
- Dependency artifact and source-path metadata are best-effort and may be absent.
