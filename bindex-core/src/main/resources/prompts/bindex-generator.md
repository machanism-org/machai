---
name: bindex-generator
description: Use when asked to generate, update, or register a Bindex-compliant bindex.json metadata file for a Java/Maven software library, based on generated Javadoc and the effective project build file. Skip for parent/aggregator (multi-module) projects.
name: bindex-generator
description: Use when asked to generate, update, or register a Bindex-compliant bindex.json metadata file for a Java/Maven software library, based on generated Javadoc and the effective project build file. Skip for parent/aggregator (multi-module) projects.
allowed-tools:
  - bash
  - read_file
  - write_file
  - get_web_content
  - register_bindex
---

# Bindex Generator

Generates a Bindex Schema v2 compliant `bindex.json` for a software library,
including practical usage examples and, when applicable, installation/configuration
instructions for ready-to-use components (CLI apps, Maven plugins, etc.).

## Applicability check

- This skill only applies to **non-parent** projects.
- Inspect the POM's `<modules>` section (or equivalent build file). If it is
  **non-empty**, this is a parent/aggregator project — stop, do not proceed.

## Workflow

### Step 1 — Build Javadoc

Run:
```bash
mvn clean javadoc:javadoc -Dshow=protected
```

If the build reports errors or warnings in the generated Javadoc:
- Apply the project's `fix-javadoc` remediation procedure.
- Retry, up to a **maximum of 3 attempts**.

### Step 2 — Gather source-of-truth material

> **IMPORTANT:** Do **not** read `.java` source files. Use only generated
> documentation and build metadata as sources of truth.

1. Generate the effective build file to a separate file, e.g. for Maven:
   ```bash
   mvn help:effective-pom -Doutput=effective-pom.xml
   ```
2. Locate the Javadoc output dir: `target/reports/apidocs`
   (fallback: `target/site/apidocs`).
3. Using an HTML-to-text fetch tool (e.g. `get_web_content`) against
   `file://` URIs, analyze:
   - `index.html` — project overview
   - `allclasses-index.html` — full class list + summaries
   - every `package-summary.html` — package-level descriptions and class reviews
4. If a `bindex.json` already exists in the project, read it first. Treat it
   as a baseline: correct/update outdated fields (version, descriptions,
   inconsistencies) rather than discarding it blindly.

### Step 3 — Generate `bindex.json`

Produce a single JSON object that:

- Strictly conforms to the Bindex Schema v2, available at:
  `https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/bindex-core/src/main/resources/schema/bindex-schema-v2.json`
- Populates **all required fields** using only information found in Step 2's sources.
- Includes optional fields when relevant information is available.
- Builds a rich `classification` property — detailed enough to be useful as
  the basis for embedding generation.
- Includes an `examples` section with **practical "how to use it" scenarios**:
  - If the library exposes a ready-to-use component (CLI, Maven plugin, etc.),
    add at least one example covering, step by step:
    1. **Install** — dependency coordinates / install command.
    2. **Configure** — config file, CLI flags, env vars.
    3. **Run** — a realistic invocation.
- Uses fully qualified class/method names in all code examples.
- Escapes every embedded `"` inside string values as `\"`.

**Output constraints:**
- The file content must be strictly valid, parsable JSON.
- No comments, no markdown, no explanatory text inside the file — the JSON object only.
- Save to `bindex.json` at the project root (overwrite after merging insights
  from any pre-existing file per Step 2.4).

### Step 4 — Register

- Call the `register_bindex` tool with the generated `bindex.json`.
- If it returns validation errors, fix `bindex.json` and retry.
- On success, report the returned `RecordId` and a clear status message
  confirming creation/update and registration.

## Response to user

Always end by informing the user of the outcome:
- Whether the task was skipped (parent project) or executed.
- Confirmation that `bindex.json` was created/updated.
- The registration `RecordId` and status.

## Configuration notes

This skill was derived from a `.toml` "act" definition (bundled at
`resources/bindex.toml`) that used templating variables from a host system:

| Variable | Meaning | Adaptation for this skill |
|---|---|---|
| `${public.prompt}` | Shared preamble/context text injected by the host system | Supply explicit task framing/context at invocation time if this skill is embedded in a similar templating system |
| `${public.fix_javadoc_rule}` | Shared remediation instruction | Already inlined above as "apply `fix-javadoc`, retry up to 3 times" |
| `gw.model` / `embedding.model` | Model routing config for generation/embeddings | Use whatever model configuration your host environment specifies |
| `gw.path = "glob:."` | Scope of file access (project root, recursive) | Ensure the skill's working directory is the project root |

## Bundled resources

- `resources/bindex.toml` — original act definition, kept for compatibility
  with systems that still consume the `.toml` format directly.
