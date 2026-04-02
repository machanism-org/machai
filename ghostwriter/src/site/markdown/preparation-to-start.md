---
<!-- @guidance:  
Analyze the `src/main/java/org/machanism/machai/gw/processor/Ghostwriter.java` class to extract and document all available configuration properties.  
For each property, provide its name, description, default value (if any), and usage context.
-->
canonical: https://machai.machanism.org/ghostwriter/preparation-to-start.html
---

## Ghostwriter configuration properties

The Ghostwriter CLI reads configuration from a properties file (default: `gw.properties`) and from system properties (for example: `-Dgw.home=...`). Some values can also be overridden via CLI options.

### System properties

| Property | Description | Default | Usage context |
|---|---|---|---|
| `gw.home` | Ghostwriter home directory. Used as the base directory for resolving the configuration file. | If not set: `projectDir` (from `-d/--projectDir`) if provided; else the current user directory. | Resolved by `initializeConfiguration(...)`; also written back as a system property to the resolved absolute path. |
| `gw.config` | Overrides the configuration file name/path resolved within `gw.home`. | `gw.properties` | Used by `initializeConfiguration(...)` to locate the properties file as `<gw.home>/<gw.config>`. |

### Properties from `gw.properties`

| Property | Description | Default | Usage context |
|---|---|---|---|
| `gw.model` | GenAI provider and model to use (format: `Provider:Model`, e.g., `OpenAI:gpt-5.1`). | None (required) | Read at startup; can be overridden by `-m/--model`. Used to create the processor/provider. |
| `instructions` | Optional system instructions. Each line may be plain text, a URL (`http://`/`https://`) to load, or a `file:` reference to load content from a file; other lines are used as-is. | `null` | Read at startup; can be overridden by `-i/--instructions`. Passed to `processor.setInstructions(...)`. |
| `gw.excludes` | Comma-separated list of directories (or path fragments) to exclude from processing. | `null` | Read at startup; can be overridden by `-e/--excludes`. Passed to `processor.setExcludes(...)`. |
| `acts.location` | Path to the directory containing act prompt files (Act mode). | `null` | Used only in Act mode when `-as/--acts` is not provided. Passed to `ActProcessor.setActsLocation(...)`. |
| `gw.act` | Default act prompt/act selection for Act mode. | `null` | Used only in Act mode as the default act prompt. Can be overridden by `-a/--act` (and if `-a/--act` is provided with no value, Ghostwriter prompts on stdin). |
| `gw.threads` | Degree of concurrency (worker thread count) for processing. | `null` | Read at startup; can be overridden by `-t/--threads`. Passed to `processor.setDegreeOfConcurrency(int)`. |
| `gw.scanDir` | Default scan directory or path matcher expression to scan when no `<scanDir>` CLI argument is provided. | If absent and no CLI scanDir is provided: current user directory absolute path. | Used by `resolveScanDirs(...)` to determine scan targets when `cmd.getArgs()` is empty. |
| `projectDir` (value of `ProjectLayout.PROJECT_DIR_PROP_NAME`) | Root directory used for file processing. | If not set and `-d` not provided: current user directory. | Used as the root directory for processing; logged as `Root directory:` and passed into processors. |
| `logInputs` (value of `Genai.LOG_INPUTS_PROP_NAME`) | Enables LLM request input logging to dedicated log files. | `false` | Read at startup; can be forced on by `-l/--logInputs`. Passed to `processor.setLogInputs(boolean)`. |
| `gw.interactive` | Enables interactive mode. | Not used | Declared in `Ghostwriter.java` but not referenced by the current implementation. |
| `gw.nonRecursive` | Disables recursive scanning. | Not used | Declared in `Ghostwriter.java` but not referenced by the current implementation. |
| `inputs` | Input selection/override. | Not used | Declared in `Ghostwriter.java` but not referenced by the current implementation. |
