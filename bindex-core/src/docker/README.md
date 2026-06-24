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

Local Bindex Repository provides a Docker Compose setup for running a local MongoDB Atlas-compatible repository for Bindex data. The repository includes MongoDB initialization that creates the `machanism` database, configures the `bindex` collection schema validator, and creates Atlas Search indexes including a vector search index for `classification_embedding`.

## Installation Instructions

### Prerequisites

Install the following tools before running the local repository:

- [Docker](https://www.docker.com/) with Docker Compose support.
- A shell environment capable of running `docker compose` commands.
- Network access to pull the `mongodb/mongodb-atlas-local` container image.
- The bindex-core application or service that will connect to this local MongoDB instance.

### Install required applications

1. Install Docker Desktop or Docker Engine for your operating system.
2. Confirm Docker is running:

   ```bash
   docker --version
   docker compose version
   ```

3. From the project root, confirm the Docker Compose file is available:

   ```bash
   ls src/docker/mongodb/docker-compose.yml
   ```

No additional build tool is required for the MongoDB container itself. Docker Compose pulls and runs the required image automatically.

## Usage

### Start the local MongoDB repository

Run Docker Compose from the MongoDB Docker directory:

```bash
cd src/docker/mongodb
docker compose up -d
```

This starts MongoDB Atlas Local on port `27017` and runs `init-db.js` during initialization. Persistent Docker volumes are used for MongoDB configuration, database files, and mongot data.

### Configure bindex-core

Set the environment variables required by bindex-core before starting the application:

```bash
export BINDEX_REPO_URL=mongodb://localhost:27017/?appName=machanism
export BINDEX_PASSWORD=pass
export BINDEX_USER=user
export GENAI_PASSWORD=...
export GENAI_USERNAME=...
export gw_model=CodeMie:gpt-5.4-2026-03-05
```

The default MongoDB credentials match the values configured in `src/docker/mongodb/docker-compose.yml`:

- Username: `user`
- Password: `pass`
- Database: `machanism`
- Collection: `bindex`

### Verify the container is running

```bash
docker compose ps
```

To view logs:

```bash
docker compose logs -f mongodb
```

### Stop the local repository

```bash
docker compose down
```

To remove the persistent data volumes as well, run:

```bash
docker compose down -v
```

Use `docker compose down -v` only when you want to delete the local MongoDB data and reinitialize the repository from scratch.
