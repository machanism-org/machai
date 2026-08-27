# GW Maven Plugin Execution

This document describes how the `gw-maven-plugin` Maven adapter executes its four goals and delegates work to the Ghostwriter processors. It is based on the implementation under `src/main/java/org/machanism/machai/gw/maven`.

## 1. Goals and execution models

| Goal | Mojo class | Aggregator | Requires project | Module traversal owner |
|---|---|---:|---:|---|
| `gw:gw` | `GWMojo` | Yes | No | `GuidanceProcessor` |
| `gw:gw-per-module` | `GWPerModuleMojo` | No | Yes | Maven reactor |
| `gw:act` | `ActMojo` | Yes | No | `ActProcessor` |
| `gw:act-per-module` | `ActPerModuleMojo` | No | Yes | Maven reactor, with processor recursion disabled per invocation |

All goals are declared thread-safe and request compile-plus-runtime dependency resolution. The aggregator goals run once at the execution root and let Ghostwriter discover/process modules. The per-module goals are invoked by Maven for each selected reactor project and deliberately prevent the Ghostwriter processor from recursively processing child modules.

There are two processing modes:

- **Guidance mode** (`gw`, `gw-per-module`) scans selected files for embedded guidance comments and uses `GuidanceProcessor` to carry out those instructions.
- **Act mode** (`act`, `act-per-module`) resolves a named reusable act or direct prompt and uses `ActProcessor` to apply it to selected files.

## 2. Common configuration resolution

All goals inherit `AbstractGWMojo.getConfiguration()`. Configuration is assembled before a processor is created.

### 2.1 Maven settings are mandatory

The injected `${settings}` object must be available. If it is `null`, execution fails with `MojoExecutionException("Maven settings are not available.")`.

### 2.2 Configuration through `genai.serverId`

When `genai.serverId` is set:

1. The plugin looks up a matching `<server>` in Maven `settings.xml`.
2. A missing server immediately fails execution.
3. A nonblank server username is stored under the AI provider username property.
4. A nonblank server password is stored under the AI provider password property.
5. If the server configuration is an `Xpp3Dom`, every direct child element is copied into the processor configuration as a key/value pair.

Example:

```xml
<server>
  <id>my-ai-provider</id>
  <username>provider-user</username>
  <password>provider-secret</password>
  <configuration>
    <AUTH_URL>https://provider.example/auth</AUTH_URL>
    <TIMEOUT>60</TIMEOUT>
  </configuration>
</server>
```

```bash
mvn gw:gw -Dgenai.serverId=my-ai-provider
```

When a server ID is used, the plugin does not load the normal Ghostwriter properties file in this method.

### 2.3 Configuration through `gw.config`

When no server ID is supplied, the plugin attempts to load:

1. the absolute path of `gw.config`, if explicitly configured; otherwise
2. the default Ghostwriter configuration filename defined by `GWConstants.GW_CONFIG_FILE_NAME`.

Failure behavior differs by source:

- Failure to read an explicitly supplied `gw.config` causes a `MojoExecutionException`.
- Failure to read the implicit default file is ignored, allowing downstream defaults, environment configuration, or provider behavior to apply.

### 2.4 Additional `params`

Plugin `<params>` are copied into the configurator after server/file loading. Consequently, a `params` entry with the same key overrides the value loaded earlier.

```xml
<configuration>
  <params>
    <endpoint>https://api.example.test</endpoint>
    <timeout>30</timeout>
  </params>
</configuration>
```

### 2.5 Important effective-value rules

The exact precedence depends on the goal and property:

- Aggregator `gw` and both act goals obtain the model with `configuration.get(gw.model, mojoModel)`. A configured value is preferred according to the configurator's lookup semantics, with the Maven-injected `model` field as fallback.
- `gw-per-module` passes the Maven-injected `model` field directly to its processor.
- `params` are applied last while building the common configurator.
- Act exclusions first check the configurator's comma-separated `gw.excludes`; if absent, the Maven-injected `excludes` array is used.
- Act scan path resolution is described separately below because it differs from guidance scanning.

## 3. Common guidance-scanning pipeline

`AbstractGWMojo.scanDocuments(GuidanceProcessor)` is used by `gw` and `gw-per-module`.

The sequence is:

1. Read `project.getBasedir()`; if it is `null`, use the operating system user directory.
2. Apply the inherited `excludes` array to the processor.
3. If `instructions` is non-null:
   - log an abbreviated form;
   - pass the complete value to the processor.
4. Resolve the Maven execution-root directory from the session.
5. If `gw.path` is absent, default the path to that execution-root directory.
   - `gw-per-module` changes `path` to the current module directory before entering this common method, so its effective default is module-local.
6. If Maven reports that a project is present, register `ClassFunctionalTools` with the processor.
7. Invoke `processor.scanDocuments(projectBasedir, path)`.
8. On any exception:
   - log the original exception;
   - wrap it in `MojoExecutionException("File processing failed.")`.
9. In all cases, log AI usage statistics and the final completion message.

The first argument to `scanDocuments` supplies project context; the second is the actual scan selection and can be a file, directory, or processor-supported pattern.

## 4. `gw:gw` execution

`GWMojo` is the aggregator guidance goal and can be invoked without a normal project POM.

### 4.1 Initialization

`execute()` performs these steps:

1. Initialize `UsageStatistics`.
2. Build the common `PropertiesConfigurator`.
3. Resolve the effective model.
4. Create a `GuidanceProcessor` rooted at the injected `basedir`.

### 4.2 Maven layout enrichment

The processor overrides `getProjectLayout(File)`:

1. Delegate initial layout detection to `GuidanceProcessor`.
2. Attach the current project directory to the detected layout.
3. If it is a `MavenProjectLayout`, read its effective Maven model.
4. Match that model's `artifactId` against `session.getAllProjects()`.
5. If a match exists:
   - scan that reactor project's classes when a Maven project is present;
   - replace the layout model with the Maven session's `MavenProject` model.

The match intentionally uses `artifactId` only. No match leaves the initially detected model unchanged. Multiple matching reactor coordinates cause an `IllegalStateException`, because the project cannot be selected unambiguously. Diagnostic coordinates use `groupId:artifactId:version@basedir`.

This enrichment lets downstream processing use Maven's already-built reactor metadata instead of relying only on raw POM detection.

### 4.3 Recursive and parallel behavior

The plugin computes non-recursive mode as:

```text
current project declares more than one module
AND Maven session contains only one project
```

That value is passed to `processor.setNonRecursive(...)`.

If Maven is running in parallel (for example, `mvn -T 4 gw:gw`), the Maven session's degree of concurrency is passed to the processor. Because this goal is an aggregator, Ghostwriter—not Maven's per-module mojo invocation—uses that thread count while coordinating module processing.

### 4.4 Scan and termination handling

The goal invokes the common guidance-scanning pipeline. A `ProcessTerminationException` is logged with its exit code and wrapped in a `MojoExecutionException`; therefore any explicit processor termination is reported as goal failure by this goal.

## 5. `gw:gw-per-module` execution

`GWPerModuleMojo` runs once for each Maven reactor project, in Maven's normal reactor/dependency order.

The sequence is:

1. Initialize usage statistics.
2. Capture `session.getExecutionRootDirectory()`.
3. Build common configuration.
4. Construct a `GuidanceProcessor` rooted at the execution root and using the mojo's `model` field.
5. Override project-layout detection:
   - call `ProjectLayoutManager.detectProjectLayout(projectDir)`;
   - for Maven layouts, attach `projectDir` and inject the current reactor project's model.
6. Override `processModule(...)` as a no-op. This prevents the processor from recursively processing modules; Maven owns module traversal.
7. If no path was supplied, set it to the current module's `basedir`.
8. If a Maven project is present, scan the current project's classpath and register `ClassFunctionalTools` immediately.
9. Enter the common scan pipeline. The common pipeline may register the same tool object again, depending on processor tool-registration behavior.
10. Convert explicit process termination into `MojoExecutionException`, including its exit code.

The execution-root directory is the processor root, while the default selected scan path is the current module directory. This allows shared reactor context while limiting each invocation to its module.

## 6. `gw:act` execution

`ActMojo` is the aggregator act goal. It can execute a reusable named act, a named act plus extra prompt text, or a direct prompt. Direct prompt syntax is interpreted by the downstream `ActProcessor`; conventionally it begins with `>`.

### 6.1 Processor construction

The sequence begins as follows:

1. Build common configuration.
2. Read optional `gw.interactive` from the configurator.
3. Resolve and log the effective model.
4. Create an `ActProcessor` rooted at `basedir`.

The processor overrides:

- `getProjectLayout(File)` to attach the directory and replace Maven layout models with matching reactor models;
- `input()` to obtain interactive processor input through Maven's Plexus `Prompter`.

For Maven layout enrichment, `ActMojo` iterates all session projects, scans each project's classes while looking for the matching `artifactId`, and stops at the first matching artifact. Unlike `GWMojo`, it does not detect duplicate artifact IDs.

### 6.2 Initial processor options

Before processing:

- A directly supplied `gw.path` is copied into the processor's act-property map.
- Non-recursive mode is inferred using the same module/session-size condition as `gw:gw`, guarded for a null project.
- Maven parallelism is translated to the processor thread count.
- A configured interactive flag is applied.
- Non-null additional instructions are logged in abbreviated form and passed to the processor.

### 6.3 Act processing setup

`process(ActProcessor)` then:

1. Initializes usage statistics.
2. Resolves the acts location from the configurator, falling back to the mojo's `gw.acts` field.
3. Applies a custom acts location when present.
4. Reads comma-separated `gw.excludes` from the configurator.
5. Uses those configured exclusions when present; otherwise uses the inherited Maven `excludes` array.
6. Calls `configureAndScan(...)`.
7. Wraps `IOException` in `MojoExecutionException`.
8. Always logs usage statistics.

### 6.4 Act prompt resolution

`configureAndScan()` resolves the act as follows:

1. If the mojo's `gw.act` field is non-null, use it and log it.
2. Otherwise call `applyActPrompt(...)` and then read `gw.act` from Maven session user properties.
3. Pass the result to `actProcessor.setAct(...)`.
4. Scan only when the resolved act is non-null.

`applyActPrompt(...)` is synchronized on a static monitor so parallel module-related activity does not prompt multiple users concurrently. Inside the synchronized section:

1. Check Maven session user properties for an already saved `gw.act`.
2. If absent, check the assembled configurator.
3. If still absent, prompt interactively with `Act`.
4. If the normalized input equals the processor's exit command, return without saving an act.
5. Otherwise save the act in session user properties so subsequent accesses reuse it.

A `PrompterException` becomes a `MojoExecutionException`.

### 6.5 Multi-line interactive input

`readText(prompt)` repeatedly calls the Plexus prompter. If a line ends with `GWConstants.MULTIPLE_LINES_BREAKER`, the breaker is removed, a line separator is appended, and prompting continues with an indented prompt. The first line without the breaker ends input collection.

This method serves both initial act collection and later interactive input requested by `ActProcessor`.

### 6.6 Act scan path resolution

The act-specific `scanDocuments(ActProcessor)` uses this precedence:

1. the mojo's `path` field;
2. `gw.path` from the processor configurator;
3. the mojo `basedir` absolute path.

It then:

1. logs the resolved path;
2. scans the current Maven project's classes and registers `ClassFunctionalTools` when a project is present;
3. invokes `actProcessor.scanDocuments(basedir, resolvedPaths)`;
4. logs completion.

This differs from the common guidance scanner: the default for aggregator act processing is `basedir`, while aggregator guidance processing defaults its missing path to the Maven execution root.

### 6.7 Termination behavior

A `ProcessTerminationException` with exit code `0` is treated as normal completion and suppressed. A nonzero termination is rethrown.

## 7. `gw:act-per-module` execution

`ActPerModuleMojo` extends `ActMojo`, but replaces the top-level execution and forces each processor scan to be non-recursive.

### 7.1 Eligibility calculation

For each Maven module invocation, it computes:

- inferred non-recursive mode using the declared-module/session-size condition;
- whether the current module directory equals the Maven execution-root directory;
- a session user-property value under `GWConstants.NONRECURSIVE_PROP_NAME`, falling back to the inferred value.

The module is processed when either:

```text
it is the execution-root project
OR effective non-recursive mode is false
```

Otherwise the goal logs `Non-recursive mode, skip scanning modules.`

The non-recursive state is shared through Maven session user properties as invocations proceed.

### 7.2 Processor execution for an eligible module

For an eligible module:

1. Resolve and, if needed, interactively collect the act through the synchronized inherited method.
2. Build an `ActProcessor` rooted at the Maven execution root.
3. Resolve the model through common configuration.
4. Detect project layouts directly with `ProjectLayoutManager`.
5. For Maven layouts, inject the current module's Maven model.
6. Override `processModule(...)` as a no-op so the processor does not recurse into children.
7. Scan/register current-project classes when a project is present.
8. Invoke inherited `process(...)`, which applies acts location, exclusions, prompt, and scan behavior.
9. Suppress a zero exit-code termination; rethrow nonzero termination.

### 7.3 Forced non-recursive scan

Immediately before the inherited act scan:

1. Save the processor's previous `isNonRecursive()` value in Maven session user properties.
2. Force `actProcessor.setNonRecursive(true)`.
3. Call the inherited act scan.

Thus Maven determines which module invokes the mojo, while each invocation prevents `ActProcessor` from traversing child modules itself.

## 8. Java class-introspection tools

When Maven reports that a project is present, the plugin can expose `ClassFunctionalTools` to Ghostwriter. The tool cache is keyed by Maven project base directory and is built from each project's classpath/output metadata.

Two AI-callable tools are registered:

- `find-class`: matches Java simple class names using a regular expression and returns fully qualified names. It rejects more than ten matches and asks the caller to refine the pattern.
- `get-class-info`: loads a fully qualified class and returns structured metadata, including modifiers, superclass, interfaces, non-private fields and methods, constructors, annotations, class location, artifact coordinate when available, and source path when available.

The cache describes the project state at scan time. Generated or modified classes are not automatically reflected unless the project is scanned again.

## 9. Path, recursion, and order summary

| Goal | Default scan path | Recursion behavior | Ordering |
|---|---|---|---|
| `gw:gw` | Maven execution root | Processor traverses unless inferred non-recursive | Ghostwriter's aggregator traversal; documented intent is children before parents |
| `gw:gw-per-module` | Current module `basedir` | Processor module callback disabled | Standard Maven reactor order |
| `gw:act` | Mojo `basedir` | Processor traverses unless inferred non-recursive | Ghostwriter act traversal; documented intent is children before parents |
| `gw:act-per-module` | Inherited act resolution, normally current `basedir` | Forced non-recursive per scan | Standard Maven reactor invocation, subject to eligibility logic |

For aggregator goals, `mvn -T N` supplies `N` to the processor. For per-module goals, Maven itself schedules mojo invocations; those goals do not copy the Maven thread count into their processors.

## 10. Error and usage-statistics behavior

- Configuration errors are surfaced as `MojoExecutionException`.
- Common guidance scanning wraps all processor exceptions as `File processing failed.` after logging the cause.
- Act processing specifically wraps `IOException`; other runtime failures propagate.
- `gw` and `gw-per-module` turn explicit process termination into Maven goal failure.
- `act` and `act-per-module` treat exit code `0` as successful early termination and propagate nonzero termination.
- Guidance scanning logs usage in the common scanner's `finally` block.
- Act processing logs usage in `ActMojo.process(...)`'s `finally` block.
- Some entry points initialize statistics before processor creation, while `ActMojo.process(...)` initializes immediately before act processing.

## 11. End-to-end examples

### Guidance processing across a project

```bash
mvn -T 4 gw:gw \
  -Dgw.path=src \
  -Dgw.instructions="Preserve public API compatibility" \
  -Dgw.excludes=target,node_modules \
  -Dgenai.serverId=my-ai-provider
```

Execution resolves credentials, creates one aggregator processor, maps Maven reactor metadata into detected layouts, applies four worker threads, registers class tools where Maven projects are available, and scans the selected path for guidance-tagged files.

### Guidance processing controlled by Maven reactor

```bash
mvn gw:gw-per-module -Dgw.path=src/main/java
```

Maven invokes the goal for each selected module in reactor order. Each invocation injects that module's Maven model, scans only its selected path, and disables processor-driven child-module handling.

### Named act

```bash
mvn -T 4 gw:act -Dgw.act=review -Dgw.acts=./acts
```

The aggregator creates an act processor, selects the custom act location, resolves `review`, adopts Maven's parallelism, and lets the processor coordinate project traversal.

### Direct act prompt per Maven module

```bash
mvn gw:act-per-module -Dgw.act=">Update module documentation" -Dgw.path=src/site
```

Maven invokes the goal per module. The act is shared through normal Maven configuration/session context, and each actual processor scan is forced to be non-recursive so child modules are not processed twice.

## 12. Responsibility boundary

This plugin is an adapter. Its source controls Maven integration, configuration assembly, prompt collection, path selection, project-layout enrichment, class-tool registration, recursion flags, concurrency settings, and exception translation. The lower-level details of matching guidance tags, loading act definitions, selecting files from patterns, constructing AI requests, applying generated changes, and the exact child-before-parent traversal algorithm belong to the `ghostwriter` dependency's `GuidanceProcessor` and `ActProcessor` implementations rather than this Maven module.
