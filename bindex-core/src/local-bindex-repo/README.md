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

Local Bindex Repository is a Docker Compose-based local MongoDB Atlas-compatible repository for Bindex metadata. It provides a `mongodb/mongodb-atlas-local` service plus initialization files for document validation, Atlas Search-style indexing, and vector search over `classification_embedding` values.

## Installation Instructions

### Prerequisites

Install these tools before starting the local repository:

- [Docker Engine](https://docs.docker.com/engine/install/) or [Docker Desktop](https://docs.docker.com/desktop/)
- Docker Compose V2, available as the `docker compose` command
- Git, if you need to clone or update this repository

No language-specific build tool is required. Docker Compose pulls the MongoDB Atlas Local image automatically when the stack starts.

### Install required applications

1. Install Docker for your operating system:
   - Windows or macOS: install Docker Desktop and start it.
   - Linux: install Docker Engine and the Docker Compose plugin using Docker's official repository instructions or your distribution package manager.
2. Verify Docker and Docker Compose are available:

   ```sh
   docker --version
   docker compose version
   ```

3. Ensure local port `27017` is available. The compose stack maps MongoDB to this port.

## Usage

Run all commands from the `src/local-bindex-repo` directory.

### Configure bindex-core environment variables

If you are running `bindex-core` against this local repository, export the required environment variables before starting the application:

```bash
export BINDEX_REPO_URL=mongodb://localhost:27017/?appName=machanism
export BINDEX_PASSWORD=pass
export BINDEX_USER=user
export GENAI_PASSWORD=...
export GENAI_USERNAME=...
export gw_model=CodeMie:gpt-5.4-2026-03-05
```

### Start the local repository

```sh
cd src/local-bindex-repo
docker compose up -d
```

Docker Compose starts a `mongodb` service with these settings:

- Image: `mongodb/mongodb-atlas-local`
- Hostname: `mongodb`
- Local port: `27017`
- Root username: `user`
- Root password: `pass`
- Initialization directory: `./init` mounted into `/docker-entrypoint-initdb.d`
- Persistent Docker volumes: `db`, `configdb`, and `mongot`

### Check service status

```sh
docker compose ps
docker compose logs -f mongodb
```

### Connect to MongoDB

Use this local connection string with MongoDB tools or application configuration:

```text
mongodb://user:pass@localhost:27017/?authSource=admin
```

For `bindex-core`, use the environment variables shown above so the application can provide the configured username and password separately.

### Stop the local repository

```sh
docker compose down
```

To stop the service and remove persisted database volumes, run:

```sh
docker compose down -v
```

Use `down -v` only when you intentionally want to delete all locally stored MongoDB data for this repository.

## Repository Contents

- `docker-compose.yml` - Defines the local MongoDB Atlas-compatible service and persistent volumes.
- `init/schema-machanism-bindex-mongoDBJSON.json` - MongoDB JSON schema for Bindex documents.
- `init/search-index-schema.json` - Search index schema with dynamic mappings enabled.
- `init/vector-index-schema.json` - Vector index schema for `classification_embedding` using cosine similarity and 700 dimensions.
