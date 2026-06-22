<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title: Local Bindex Repository**  
   - Provide the project name and a brief description based on folder content summary.
2. **Installation Instructions:**  
   - Describe prerequisites and build tools.
   - Describe how to install all required application to build and run docker compose.
3. **Usage:**  
   - Explain how to build and run the bindex repository on the local machine.
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

# Local Bindex Repository

Local Bindex Repository provides a Docker Compose setup for running a local MongoDB Atlas-compatible repository used to store and search Bindex metadata. The repository includes MongoDB initialization assets for collection validation and Atlas Search-style indexes, including a vector index over `classification_embedding` values.

## Installation Instructions

### Prerequisites

Install the following tools before running the repository locally:

- [Docker Engine](https://docs.docker.com/engine/install/) or [Docker Desktop](https://docs.docker.com/desktop/)
- Docker Compose V2, available as the `docker compose` command
- Git, if you need to clone or update the source repository

### Install required applications

1. Install Docker for your operating system:
   - Windows or macOS: install Docker Desktop from the Docker documentation.
   - Linux: install Docker Engine and the Docker Compose plugin using your distribution package manager or Docker's official repository instructions.
2. Start Docker and verify that it is available:

   ```sh
   docker --version
   docker compose version
   ```

3. Ensure port `27017` is available on your local machine. The compose file exposes MongoDB on this port.

No additional build tool is required. The service uses the `mongodb/mongodb-atlas-local` image and Docker pulls it automatically when the stack starts.

## Usage

Run all commands from the `src/bindex-repo` directory.

### Start the local repository

```sh
cd src/bindex-repo
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

Use the following local connection string with MongoDB tools or application configuration:

```text
mongodb://user:pass@localhost:27017/?authSource=admin
```

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
