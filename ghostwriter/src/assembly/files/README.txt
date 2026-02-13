Ghostwriter CLI (gw)
=====================

Application Overview
--------------------
Ghostwriter is a command-line documentation engine that scans a project tree, extracts embedded `@guidance` blocks from documentation files, and uses a configured GenAI provider/model to synthesize, review, or update content. It is designed to help teams keep documentation accurate and consistent by generating updates directly from the source tree and the rules embedded in documentation files.

Key features:
- Scans directories or patterns (including `glob:` / `regex:` style inputs) and processes supported document types
- Uses embedded `@guidance` tags to drive consistent, repeatable documentation output
- Optional default guidance and system instructions supplied inline, via URL, or from local files
- Multi-threaded processing for faster runs on large trees
- Optional logging of LLM request inputs for auditability and debugging

Typical use cases:
- Keep README / docs aligned with code changes
- Enforce documentation standards via embedded guidance
- Batch review or regeneration of docs across large repositories

Supported GenAI providers:
- CodeMie
- OpenAI and OpenAI-compatible services (via API key + optional base URL)

Installation Instructions
-------------------------
Prerequisites:
- Java 11+ (JRE or JDK)
- Network access to your configured GenAI provider (if applicable)
- Provider credentials/configuration (see `gw.properties`)

Install / obtain the CLI:
- Download the Ghostwriter CLI bundle:
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

Bundle contents (this folder):
- `gw.jar` (the CLI executable JAR; referenced by the scripts)
- `gw.bat` (Windows launcher)
- `gw.sh` (Unix/macOS launcher)
- `gw.properties` (example configuration)

Configuration basics:
- Edit `gw.properties` to choose a provider/model and set credentials.
- You can also override properties via environment variables or Java system properties (`-D...`).

How to Run
----------
Direct (Windows):
```bat
java -jar gw.jar C:\projects\my-project
```

Using the provided scripts:

Windows (`gw.bat`):
```bat
gw.bat C:\projects\my-project
```

Unix/macOS (`gw.sh`):
```sh
./gw.sh /path/to/my-project
```

Common configuration methods:

1) Use a properties file
- Place `gw.properties` next to `gw.jar` or point to it explicitly:

Windows:
```bat
java -Dgw.config=gw.properties -jar gw.jar C:\projects\my-project
```

Unix/macOS:
```sh
java -Dgw.config=gw.properties -jar gw.jar /path/to/my-project
```

2) Environment variables (examples)
- CodeMie (used by `gw.bat` / `gw.sh` examples):

Windows:
```bat
SET GENAI_USERNAME=your_codemie_username
SET GENAI_PASSWORD=your_codemie_password
```

Unix/macOS:
```sh
export GENAI_USERNAME=your_codemie_username
export GENAI_PASSWORD=your_codemie_password
```

- OpenAI-compatible:
  - `OPENAI_API_KEY`
  - `OPENAI_BASE_URL` (optional; required for non-OpenAI endpoints)

Command-line options (examples)

- Set system instructions (`-i` / `--instructions`)
  - Accepts plain text, URL, or `file:` input.

Windows:
```bat
java -jar gw.jar C:\projects\my-project -i "Review and update docs for accuracy"
```

Unix/macOS:
```sh
java -jar gw.jar /path/to/my-project -i "Review and update docs for accuracy"
```

- Set default guidance applied as a final step (`-g` / `--guidance`)

Windows:
```bat
java -jar gw.jar C:\projects\my-project -g file:C:\projects\my-project\docs\default-guidance.txt
```

Unix/macOS:
```sh
java -jar gw.jar /path/to/my-project -g file:/path/to/my-project/docs/default-guidance.txt
```

- Exclude directories (`-e` / `--excludes`)

Windows:
```bat
java -jar gw.jar C:\projects\my-project -e target,.git,node_modules
```

Unix/macOS:
```sh
java -jar gw.jar /path/to/my-project -e target,.git,node_modules
```

- Set a root directory (`-r` / `--root`)

Windows:
```bat
java -jar gw.jar -r C:\projects C:\projects\my-project
```

Unix/macOS:
```sh
java -jar gw.jar -r /projects /projects/my-project
```

Full example (matches documented options):

Windows:
```bat
java -Dgw.config=gw.properties -jar gw.jar C:\projects\my-project ^
  -a OpenAI:gpt-5.1 ^
  -t true ^
  -e target,.git,node_modules ^
  -g file:C:\projects\my-project\docs\default-guidance.txt
```

Unix/macOS:
```sh
java -Dgw.config=gw.properties -jar gw.jar /path/to/my-project \
  -a OpenAI:gpt-5.1 \
  -t true \
  -e target,.git,node_modules \
  -g file:/path/to/my-project/docs/default-guidance.txt
```

Troubleshooting & Support
--------------------------
Common issues:
- Authentication failures
  - Verify the correct provider is selected (`genai=...` in `gw.properties` or `-a Provider:Model`).
  - Ensure required credentials are set:
    - CodeMie: `GENAI_USERNAME` / `GENAI_PASSWORD`
    - OpenAI-compatible: `OPENAI_API_KEY` (+ `OPENAI_BASE_URL` if using a compatible endpoint)

- Configuration not being picked up
  - Confirm `gw.properties` is in the expected location, or use `-Dgw.config=<path>`.
  - If overriding by environment variables / `-D...`, ensure names match the properties you expect to set.

- Nothing happens / wrong files processed
  - Confirm the root path argument points at the intended project folder.
  - Use `-e` / `--excludes` to skip build output and VCS folders (e.g., `target,.git,node_modules`).
  - If using patterns (e.g., `glob:` / `regex:`), validate the pattern matches the intended files.

Logs and debug:
- Enable LLM input logging with `-l` / `--logInputs` to write request inputs to dedicated log files.
- If you run into provider/network issues, check your network connectivity and proxy settings.

Contact & Documentation
-----------------------
- Project repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Downloads: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
