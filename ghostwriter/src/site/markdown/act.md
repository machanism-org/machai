<!-- @guidance: 
Update this page: "The Act" as a Project Information page for the project:
- Analyze `src/main/java/org/machanism/machai/gw/processor/package-info.java` 
- Analyze `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java`, `src/main/java/org/machanism/machai/gw/processor/AIFileProcessor.java`. 
  classes and `src/main/resources/acts` files as toml act file examples.
- Note: `enabledTools` can have regular expression patterns controlling which tools are registered. Each tool is identified internally by a fully qualified name built in the format <ClassName>:<toolName> , where:
	<ClassName> is the fully qualified class name of the tools implementation, and <toolName> is the tool's declared name (or the method name if no explicit name is provided).
	A tool is registered only if its fully qualified name matches at least one of the given patterns. If enabledTools is null , all available tools are registered without filtering.  
- Write a general description of the Act feature and its main functionality, using clear and simple language suitable for users who may not have prior technical knowledge or experience with the project.
- Create a separate section describing the action's interactive/non-interactive mode.
  - An action can be used as a non-interactive command to perform a predefined task without any additional data.
  - An action can be used interactively (as a chat). This is necessary when the user does not have full information about the desired action before initiating it.
  - Describe how to use value of `AIFileProcessor.EXIT_SPECIAL_PROMPT_COMMAND` and `AIFileProcessor.CONTINUE_SPECIAL_PROMPT_COMMAND` to continue or terminate the act processing.
  - Describe how to use value of ``.
— Create a special section describing how to use the `prompt` property in the toml file to set a default value for the user's prompt. This will be used if the user doesn't provide a prompt.
— Create a dedicated section describing how to use the `episode` feature and `input parameters` definition described in `AIFileProcessor` class.
- Clearly describe how inherited values are processed within the file:
  - Explain the mechanism by which values can be inherited from parent sections, templates, or defaults.
  - Specify how and when these inherited values are applied or overridden in the context of the Act TOML configuration.
  - Provide examples if relevant, to illustrate the inheritance process. 
— Create a dedicated section describing how to use `enabledTools: auto` feature.
- Summarize the purpose of the Act feature, its key methods, and how it fits into the overall project.
- Provide easy-to-follow, step-by-step instructions or a practical example showing how to use the Act feature in real scenarios.
- Ensure the content is accessible and helpful for all users, including those new to the codebase or without a technical background.
- Analyze all act TOML files located in the `src/main/resources/acts` folder.
- For each act, create a section that includes:
  - The act's name.
  - A clear, concise description of the act's purpose and when it should be used.
- Describe the usage of placeholder variables, which should be used in the ${...} format. 
  Placeholders of this type are intended for dynamic substitution by functional tools at runtime. 
  The LLM must not alter, resolve, or modify these placeholders in any way. 
  They are designed to enable retrieval of parameters from the configurator, 
  such as environment variables, system properties, action properties, and similar sources.
- An absolute path to a TOML file can be used as the file name; in this case, hierarchy using  classpath resources on the is not supported.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
- Include a link to [Act-Driven Workflows (ADW)](https://machanism.org/act/index.html) for users who want to learn more.
-->

# The Act

An **Act** is a reusable Ghostwriter workflow. Instead of writing the same request and tool setup every time, you select an act by name. Its TOML file supplies instructions, prompts, optional file-selection settings, and tool access. Ghostwriter then applies the workflow to the current project, a folder, or matching files.

Acts sit on top of `AIFileProcessor`: `ActProcessor` loads and combines the TOML configuration, prepares the user request and episodes, and delegates each prompt to the AI file processor. The file processor supplies project and file context, loads permitted tools, expands supported prompt content, and calls the configured AI provider. This makes acts suitable for routine work such as creating documentation, generating tests, reviewing SonarQube findings, or running a carefully scoped custom task.

For a broader introduction to this style of automation, see [Act-Driven Workflows (ADW)](https://machanism.org/act/index.html). To learn how modules and individual project files are traversed and processed while an act's episodes run, see [Module & Project File Processing during Act Steps](module-file-act-processing.html).

## Quick start

1. Choose a built-in act, for example `unit-tests`, or create `my-review.toml` in your configured acts directory.
2. Run the act by name and optionally add a request after it. For example: `unit-tests Focus on the parser package`.
3. The text after the name is the requested task. When the act has not already supplied `public.prompt` (including through a default), it becomes the user prompt available to the TOML template as `${public.prompt}`.
4. The act loads its instructions, tools, inputs, defaults, and any parent act. It then processes the selected project files or project-level episodes.
5. Review the result. In interactive acts, continue the conversation or end it using the commands described below.

A command beginning with `>` is shorthand for the generic `task` act. For example, `> summarize the project structure` is treated as `task summarize the project structure`. If no act is named, Ghostwriter uses `help`.

## Act TOML anatomy

A simple act has an optional description, instructions for the AI, and one or more inputs:

```toml
description = "Review a project area."

instructions = '''
Explain findings clearly and make only necessary changes.
'''

inputs = '''
---
enabledTools:
  - org.example.ProjectTools:read_project
---
Review ${public.prompt}.
'''

[default]
public.prompt = "the project structure"

[gw]
interactive = true
```

`instructions` provide stable, system-level guidance. `inputs` hold the prompt sent for an act or episode. `[gw]` settings configure Ghostwriter behavior, including `path`, `interactive`, `threads`, `excludes`, and `nonRecursive`. Other string settings are made available through the configurator. A TOML `inputs` value can be a single string or an array of strings.

### Default user prompt: the `prompt` property

The prompt property used by an act is the dotted TOML key `public.prompt` (not a separate top-level `prompt` key). Set `default.public.prompt` in the TOML file to provide the default request used when no direct `public.prompt` value has already been configured:

```toml
[default]
public.prompt = "Review the project for documentation gaps."

inputs = '''
Perform this request: ${public.prompt}
'''
```

The resulting value is stored as `public.prompt` and can be inserted anywhere in the prompt with `${public.prompt}`. A value already supplied as `public.prompt` is retained rather than replaced. Defaults are applied before the request text appended to the act name is bound. Consequently, an act that defines `default.public.prompt` retains that default unless the caller supplies `public.prompt` through configuration; appended request text does not replace it. If an act must use the text appended after its name, do not set a competing `default.public.prompt`, or arrange for the caller to set `public.prompt` explicitly.

### Prompt front matter and tools

An input may start with YAML front matter between `---` lines. `gw.model` selects a model/provider for that prompt. `enabledTools` can be a whitespace-, comma-, or semicolon-separated value or a YAML list:

```toml
inputs = '''
---
gw.model: ${public.reviewModel}
enabledTools:
  - org.machanism.machai.gw.tools.ProjectContextFunctionTools:get_project_context_variable
  - org\.machanism\..*:read_file
---
Inspect ${public.prompt}.
'''
```

Each `enabledTools` item is a regular-expression pattern. A tool is identified internally as `<ClassName>:<toolName>`—for example, `org.example.Tools:read_file`; the tool name is its declared name, or the method name when no explicit name exists. A tool is registered only when its full identifier matches at least one pattern. If `enabledTools` is `null` or omitted, all available tools are registered without filtering. Use precise patterns when an act needs limited capabilities.

Prompt and instruction lines can include content using `>>> URL` or `>>> file://relative/path`; included UTF-8 content is processed recursively. Public configuration values can be substituted in prompt metadata and text.

### Automatic tool selection: `enabledTools: auto`

An act can ask Ghostwriter to choose tools for an episode instead of maintaining a fixed pattern list. Put `enabledTools: auto` in that episode's YAML front matter:

```toml
inputs = '''
---
enabledTools: auto
---
Review the selected files and make the required changes.
'''
```

Before running the episode, Ghostwriter asks the configured AI provider to select the relevant tools from the available set using the act instructions and episode prompt. The resulting selection is cached for that act and episode, then only those tools are registered for the actual request. To give the selector an additional constraint, use an `auto` mapping:

```toml
inputs = '''
---
enabledTools:
  auto: Don't use web access and system command tools.
---
Analyze the local implementation.
'''
```

The mapping value guides selection; it is not a hard tool denylist. Use explicit regular-expression patterns when a strict, predictable tool boundary is required.

### Placeholders

Write dynamic placeholders exactly as `${...}`, such as `${public.prompt}`, `${sonar.host}`, `${sonar.token}`, and `${super.value}`. They are runtime substitution points for the configurator and functional tools: environment values, system properties, action properties, and similar configured sources can provide their values. The LLM must not edit, resolve, rename, or otherwise modify these placeholders in an act template; they must remain intact until the runtime component performs substitution.

## Episodes and input parameters

When `inputs` is an array, every item is an **episode**: an ordered prompt in one larger workflow. This supports staged work—for example, inspect first, make changes second, and validate third. An act can also use a single input, which becomes the default prompt.

Select particular episodes by adding `#` to the act name. Episode numbers are comma-separated, and `!` prevents normal sequential processing after the requested episodes:

```text
review#1,3! Check concurrency and error handling
```

This runs episodes 1 and 3 with the supplied request, then stops rather than continuing in normal order. Without `!`, selected episodes run and normal ordering may continue. Episodes can also request movement to another episode during processing.

The YAML input parameters recognized specially by `AIFileProcessor` are:

- `gw.model` — overrides the configured AI model/provider for the prompt.
- `enabledTools` — a string or YAML list of regular-expression patterns controlling registered tools.
- Other YAML values are retained as prompt configuration; string values are resolved through the active configurator, but they do not by themselves change processing behavior.

For every processed item, Ghostwriter also sends JSON process information containing `PROCESSED_FILE_REL_PATH`, `PROCESS_MODE` (`INTERACTIVE` or `NOT-INTERACTIVE`), and `OS_NAME`. Project-context tools receive the project name, IDs, directories, source/test/document folders, modules, and operating-system context.

For a detailed look at how an episode's file-selection settings (such as `path`, `excludes`, and `nonRecursive`) determine which modules and files are visited, and in what order, see [Module & Project File Processing during Act Steps](module-file-act-processing.html).

## Interactive and non-interactive acts

A non-interactive act is a predefined command: it can run from its configuration without asking for extra data. This is useful for repeatable tasks such as generating documentation or tests. Add request text only when you need to narrow or customize the work.

Set `gw.interactive = true` when the act should operate as a chat. Interactive mode is useful when the desired action is not fully known at the beginning: the AI responds, then you can provide the next instruction. After a response:

- Enter `>` (`AIFileProcessor.CONTINUE_SPECIAL_PROMPT_COMMAND`) to accept the current response and continue processing without another AI prompt.
- Enter `.` (`AIFileProcessor.EXIT_SPECIAL_PROMPT_COMMAND`) to terminate the act successfully.
- Enter `>>` to accept the current response and switch the remaining work to non-interactive processing.
- An empty entry has no special command meaning; when the hosting environment supplies it, it is treated as ordinary follow-up input rather than as a continue or exit command.
- Enter any other text to send it as the next chat prompt.

Interactive input requires an environment that supports it. In a non-interactive execution environment, acts proceed without chat input; use a predefined non-interactive act when the task must run unattended.

## Inheritance, overrides, and defaults

An act may inherit another act using `basedOn`:

```toml
basedOn = "base-review"

instructions = "${super.value}\nAlso check public APIs."
inputs = [
  "${super.value}\nReport a concise summary.",
  "Run validation."
]

[default]
gw.threads = 2
```

The child definition is read and its `basedOn` parent is then loaded into the same property map. During this merge, the value already in the map is the overriding value. For strings, `${super.value}` in that overriding value is replaced with the inherited value; without the placeholder, the overriding value remains unchanged. Input arrays are handled by position: an entry containing `${super.value}` incorporates the corresponding inherited episode, while entries beyond the other array are kept. A custom act with the same name as a built-in act is also loaded and can override or extend the bundled definition.

`[default]` entries are fallbacks. A `default.some.key` value is copied to `some.key` only when no non-default act value exists; an existing configurator value can replace that fallback. When string act data is later applied, `${super.value}` can also be resolved from the current configurator value. In practice: use `basedOn` for reusable templates, `${super.value}` to preserve or append inherited/configured content, and `[default]` for values that should be available only when no direct value was set.

For example, this child keeps the parent's instructions and adds one rule:

```toml
basedOn = "base-review"
instructions = '''
${super.value}
Also check public APIs.
'''
```

If `base-review` says `Review carefully.`, the effective instruction includes both sentences. Replacing the child value with `instructions = "Check public APIs."` deliberately discards the inherited instruction.

## Create and run a custom act

1. In the configured acts directory, create `doc-audit.toml`.
2. Add a description, stable `instructions`, an `inputs` prompt, and any `[gw]` settings. Keep runtime variables such as `${public.prompt}` exactly as written.
3. Start with a small, safe scope and tools that the act genuinely needs:

   ```toml
   description = "Checks Markdown documentation for clarity."
   instructions = "Explain suggestions in plain language."
   inputs = '''
   ---
   enabledTools:
     - org\.machanism\..*:read_file
   ---
   Review the Markdown files in ${public.prompt}.
   '''

   [gw]
   path = "glob:src/site/**/*.md"
   interactive = true
   ```

4. Run `--act doc-audit src/site`. Ghostwriter loads the TOML file, resolves allowed configuration values at runtime, selects matching files, and starts the workflow. Because it is interactive, use `>` to continue after a response or `.` to end it.
5. If the workflow later needs separate analysis and editing stages, change `inputs` to a TOML array; each array item becomes an episode.

## Finding act files

Built-in acts are classpath resources under `/acts/` and end in `.toml`. External acts can be loaded from a configured local acts directory or an HTTP(S) location. Relative local locations are resolved from the project root. You can also use an **absolute path to a TOML file** as the act name, for example `C:\work\acts\my-review.toml`. In that case, the file is loaded directly; classpath-resource hierarchy is not supported for that file.

## Built-in acts

### `code-doc`

Adds or improves documentation comments in code files, using the language-appropriate format such as Javadoc or docstrings. Use it when documentation is missing, unclear, or incomplete; it is limited to documentation changes and ignores `.machai`.

### `commit`

Analyzes pending Git or SVN changes, groups them into logical commits, creates messages matching the repository style, and executes the commits. Use it only when you want the act to stage and commit project changes automatically; it runs interactively and uses command tools.

### `grype-fix`

Uses Syft and Grype scan output to find dependency vulnerabilities, update dependencies with available fixes, build the Maven project, and document remediation. Use it when Syft and Grype are installed and dependency security findings need practical fixes.

### `help`

Provides interactive help for Ghostwriter acts and episodes, including how to locate an act, inspect its properties, understand inheritance, and invoke it. Use it when you are unsure which act to choose or how an existing act is configured.

### `sonar-fix`

Retrieves SonarQube findings, fixes eligible quality and security issues, adds or updates tests, validates the build, and records changed files in `UPDATED_FILES_REPORT`. Use it for an available, configured SonarQube service; it deliberately does not change SonarQube configuration or quality gates.

### `task`

Provides the minimal general-purpose project-aware assistant workflow. Use it for a custom request that does not need a specialized act; the `>` shorthand selects this act automatically.

### `unit-tests`

Builds the project, analyzes JaCoCo coverage, and creates or improves unit tests for under-covered code. Use it when you need meaningful test coverage improvements, especially for a specified source area.

## Key API responsibilities

For integrators, `ActProcessor` is responsible for selecting an act, loading TOML from built-in, local, remote, or explicit-file sources, applying defaults and inheritance, selecting episodes, and collecting outputs through `getResults()`. `AIFileProcessor` performs provider execution, reads front matter, applies tool filtering and public-value substitution, processes includes, and produces per-file process information. Together they provide Ghostwriter's reusable, project-aware automation layer. See [Module & Project File Processing during Act Steps](module-file-act-processing.html) for more on how these two components traverse modules and files while an act runs.