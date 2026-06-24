---
<!-- @guidance:
Create a web page content that guides users on how to Bindex Repository on local env.  
The page should based on `src/docker/README.md`:
- An introduction explaining how to run dockerized mongodb as a bindex database.
- Step-by-step instructions how to compose and run image.
- Describe how to initialize database and collection with MongoDB Compass via `src/docker/mongodb/init-db.js` manually.
- How to connect to it.
- Tips for troubleshooting common issues.
- Links to relevant documentation, downloads, and support resources.
- Clear, user-friendly formatting with headings, code blocks, and actionable guidance.
-->
canonical: https://machai.machanism.org/bindex-core/local-repository.html
---

# Local Bindex Repository

You can run a local Bindex Repository by starting a Dockerized MongoDB Atlas Local instance. This is useful for development, testing, and validating Bindex data without depending on a remote MongoDB deployment.

The Docker setup under `src/docker/mongodb` runs MongoDB on `localhost:27017`, creates the `machanism` database, configures the `bindex` collection, and applies the Atlas Search indexes defined in `src/docker/mongodb/init-db.js`.

## What you will run

The local repository uses:

- `src/docker/mongodb/docker-compose.yml` to start MongoDB Atlas Local.
- `src/docker/mongodb/init-db.js` to initialize the database, collection validator, and search indexes.
- Docker volumes to persist MongoDB configuration, database files, and `mongot` data between container restarts.

Default connection details:

| Setting | Value |
| --- | --- |
| Host | `localhost` |
| Port | `27017` |
| Username | `user` |
| Password | `pass` |
| Database | `machanism` |
| Collection | `bindex` |

## Prerequisites

Install the following before starting the local repository:

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) or Docker Engine with Docker Compose support.
- A terminal or shell that can run `docker compose` commands.
- Network access to pull the `mongodb/mongodb-atlas-local` image.
- Optional: [MongoDB Compass](https://www.mongodb.com/products/tools/compass) for manual database inspection and initialization.

Verify Docker is available:

```bash
docker --version
docker compose version
```

## Start the local Bindex Repository

### 1. Open the MongoDB Docker directory

From the project root, move to the Docker Compose directory:

```bash
cd src/docker/mongodb
```

### 2. Start MongoDB Atlas Local

Run Docker Compose in detached mode:

```bash
docker compose up -d
```

Docker Compose pulls the required image if it is not already available, starts the MongoDB container, exposes port `27017`, and runs the initialization script on first startup.

### 3. Check the container status

```bash
docker compose ps
```

The MongoDB service should be listed as running.

### 4. View logs if needed

```bash
docker compose logs -f mongodb
```

Use the logs to confirm startup, initialization, or diagnose connection issues.

## Configure bindex-core to connect locally

Before starting bindex-core, set the environment variables that point the application to the local MongoDB repository:

```bash
export BINDEX_REPO_URL=mongodb://localhost:27017/?appName=machanism
export BINDEX_PASSWORD=pass
export BINDEX_USER=user
export GENAI_PASSWORD=...
export GENAI_USERNAME=...
export gw_model=CodeMie:gpt-5.4-2026-03-05
```

Then start bindex-core using your normal development workflow. The application should connect to MongoDB on `localhost:27017` using the configured username and password.

## Connect with MongoDB Compass

MongoDB Compass is the easiest way to inspect the local database and manually run initialization steps.

1. Download and install [MongoDB Compass](https://www.mongodb.com/products/tools/compass).
2. Start the Docker container with `docker compose up -d`.
3. Open MongoDB Compass.
4. Create a new connection using this connection string:

   ```text
   mongodb://user:pass@localhost:27017/?authSource=admin&appName=machanism
   ```

5. Connect and look for the `machanism` database and `bindex` collection.

If the database was initialized successfully, the `machanism.bindex` collection should already exist.

## Manually initialize the database with MongoDB Compass

The initialization script is located at:

```text
src/docker/mongodb/init-db.js
```

Docker runs this script automatically during first container initialization. If you need to apply it manually, use MongoDB Compass as follows:

1. Connect to the local MongoDB instance in Compass:

   ```text
   mongodb://user:pass@localhost:27017/?authSource=admin&appName=machanism
   ```

2. Open the Compass shell or embedded MongoDB shell.
3. Open `src/docker/mongodb/init-db.js` in your editor.
4. Copy the script content exactly as provided.
5. Paste it into the Compass shell and execute it.
6. Refresh Compass and confirm:
   - The `machanism` database exists.
   - The `bindex` collection exists.
   - The collection validator is configured.
   - Search indexes were created for the collection.

If you want to force a clean reinitialization, stop the container and remove volumes before starting it again:

```bash
docker compose down -v
docker compose up -d
```

Use `docker compose down -v` carefully because it deletes the local MongoDB data stored in Docker volumes.

## Stop the repository

To stop the container while keeping persistent data:

```bash
docker compose down
```

To stop the container and delete all persisted MongoDB data:

```bash
docker compose down -v
```

## Troubleshooting

### Port `27017` is already in use

Another MongoDB instance may already be running locally. Stop the other service or update the port mapping in `src/docker/mongodb/docker-compose.yml`.

Check what is using the port:

```bash
docker ps
```

### Compass cannot connect

Confirm the container is running:

```bash
docker compose ps
```

Then verify the connection string includes credentials and `authSource=admin`:

```text
mongodb://user:pass@localhost:27017/?authSource=admin&appName=machanism
```

### Database or collection is missing

The initialization script runs only during first database initialization. If existing volumes were already created before the script was added or updated, recreate the volumes:

```bash
docker compose down -v
docker compose up -d
```

### Authentication fails

Use the default credentials from the Docker Compose configuration:

- Username: `user`
- Password: `pass`
- Authentication database: `admin`

For application configuration, keep:

```bash
export BINDEX_USER=user
export BINDEX_PASSWORD=pass
```

### Atlas Search or vector search indexes are not available

Make sure the container image is `mongodb/mongodb-atlas-local` and not a standard MongoDB server image. Atlas Search features require the Atlas Local image used by the Docker Compose setup.

Check logs for index creation errors:

```bash
docker compose logs -f mongodb
```

## Useful links

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Docker Compose documentation](https://docs.docker.com/compose/)
- [MongoDB Atlas Local](https://www.mongodb.com/docs/atlas/cli/current/atlas-cli-deploy-local/)
- [MongoDB Compass](https://www.mongodb.com/products/tools/compass)
- [MongoDB connection string reference](https://www.mongodb.com/docs/manual/reference/connection-string/)
- [MongoDB Atlas Search documentation](https://www.mongodb.com/docs/atlas/atlas-search/)

For project-specific support, review the files in `src/docker/mongodb` and the bindex-core configuration used by your local development environment.
