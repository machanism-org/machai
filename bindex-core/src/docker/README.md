<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title: Local Bindex Repository**  
   - Provide the project name and a brief description based on folder content summary.
2. **Installation Instructions:**  
   - Describe prerequisites and build tools.
   - Describe how to install all required application to build and run docker compose.
3. **Usage:**  
   - Explain how to build and run the bindex repository on the local machine.
   - Environment variables for the bindex-core:
   ```bash
	export BINDEX_REPO_URL=mongodb://localhost:27017/?appName=machanism
    export BINDEX_PASSWORD=pass
	export BINDEX_USER=user
	export GENAI_PASSWORD=...
	export GENAI_USERNAME=...
	export gw_model=CodeMie:gpt-5.4-2026-03-05
   ```
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

# Local Bindex Repository

The `src/docker` directory provides the Docker Compose definition and initialization script for a local Bindex repository. It runs MongoDB Atlas Local, creates the `machanism` database and `bindex` collection, applies the collection schema validator, and creates the `id` and vector search indexes used by Bindex.

## Installation Instructions

### Prerequisites

Install or enable the following before starting the repository:

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) on Windows, with the WSL 2 backend enabled, or Docker Engine with the Docker Compose v2 plugin on another operating system.
- A terminal that can run Docker commands. Git Bash, WSL, or PowerShell can be used on Windows.
- Network access to pull the [`mongodb/mongodb-atlas-local`](https://hub.docker.com/r/mongodb/mongodb-atlas-local) image.
- The `bindex-core` application, if you intend to connect an application to the repository.

Docker Compose v2 is included with current Docker Desktop installations. No separate compiler, package manager, or Dockerfile build step is required: Compose pulls the MongoDB image and starts the configured service.

### Install and verify Docker

1. Install Docker Desktop and start it, or install Docker Engine and the Docker Compose v2 plugin for your operating system.
2. Verify that both Docker and Compose are available:

   ```bash
   docker --version
   docker compose version
   docker info
   ```

   `docker info` must complete successfully before the service can start.
3. From the project root, verify the Compose definition and initialization script are present:

   ```bash
   # Bash, Git Bash, or WSL
   test -f src/docker/mongodb/docker-compose.yml && test -f src/docker/mongodb/init-db.js

   # PowerShell
   Test-Path src/docker/mongodb/docker-compose.yml
   Test-Path src/docker/mongodb/init-db.js
   ```

## Usage

### Build and start the local Bindex repository

There is no local image to build. Start the MongoDB service from the project root with:

```bash
docker compose -f src/docker/mongodb/docker-compose.yml up -d
```

This command pulls `mongodb/mongodb-atlas-local` when necessary, maps host port `27017` to the container, and mounts `init-db.js` as the initialization script. The script runs when MongoDB initializes its data directory, creates or updates the `machanism.bindex` collection validator, and creates the search indexes. Named volumes persist MongoDB configuration, database, and `mongot` data between container restarts.

> Initialization scripts are only run for a new database volume. To apply initialization changes to a fresh local repository, stop the service and remove its volumes as described below.

### Configure bindex-core

Set these environment variables before starting `bindex-core`:

```bash
export BINDEX_REPO_URL=mongodb://localhost:27017/?appName=machanism
export BINDEX_PASSWORD=pass
export BINDEX_USER=user
export GENAI_PASSWORD=...
export GENAI_USERNAME=...
export gw_model=CodeMie:gpt-5.4-2026-03-05
```

`GENAI_PASSWORD` and `GENAI_USERNAME` must be replaced with valid credentials for the configured GenAI service. The local MongoDB credentials are defined in `src/docker/mongodb/docker-compose.yml`:

- Username: `user`
- Password: `pass`
- Database: `machanism`
- Collection: `bindex`

For PowerShell, set the same values with `$env:VARIABLE_NAME = "value"`, for example `$env:BINDEX_USER = "user"`.

### Check status and logs

Run these commands from the project root:

```bash
docker compose -f src/docker/mongodb/docker-compose.yml ps
docker compose -f src/docker/mongodb/docker-compose.yml logs -f mongodb
```

The service is ready when the container is running and its logs no longer report startup errors. Applications running on the host can connect through `localhost:27017`.

### Stop the repository

Stop the container while retaining local data:

```bash
docker compose -f src/docker/mongodb/docker-compose.yml down
```

To remove the containers and all named MongoDB volumes, forcing the database and indexes to be initialized again on the next start:

```bash
docker compose -f src/docker/mongodb/docker-compose.yml down -v
```

Use `down -v` only when deleting the local Bindex data is intentional.
