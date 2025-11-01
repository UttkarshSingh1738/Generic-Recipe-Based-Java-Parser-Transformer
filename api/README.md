# Recipe Transformer API

REST API layer for the Recipe-Based Java Parser Transformer platform.

## Features

- **Recipe Management**: CRUD operations for transformation recipes
- **Project Management**: Manage source code projects
- **Job Execution**: Create and monitor transformation jobs

## Endpoints

### Recipes

- `GET /api/recipes` - List all recipes
- `GET /api/recipes/public` - List public recipes
- `GET /api/recipes/search?q={term}` - Search recipes
- `GET /api/recipes/{id}` - Get recipe by ID
- `GET /api/recipes/name/{name}` - Get recipe by name
- `POST /api/recipes` - Create new recipe
- `PUT /api/recipes/{id}` - Update recipe
- `DELETE /api/recipes/{id}` - Delete recipe

### Projects

- `GET /api/projects` - List all projects
- `GET /api/projects/{id}` - Get project by ID
- `POST /api/projects` - Create new project
- `PUT /api/projects/{id}` - Update project
- `DELETE /api/projects/{id}` - Delete project

### Jobs

- `GET /api/jobs` - List all jobs
- `GET /api/jobs/{id}` - Get job by ID
- `GET /api/jobs/project/{projectId}` - Get jobs for a project
- `GET /api/jobs/status/{status}` - Get jobs by status
- `POST /api/jobs` - Create new transformation job
- `PATCH /api/jobs/{id}/status` - Update job status

## Running the Application

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

## Database

Currently configured to use H2 in-memory database for development. 
Access H2 console at: `http://localhost:8080/h2-console`

For production, configure PostgreSQL in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/recipe_db
    username: your_user
    password: your_password
```

