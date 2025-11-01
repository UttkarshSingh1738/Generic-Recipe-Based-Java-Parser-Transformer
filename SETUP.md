# Recipe Transformer - Setup Guide

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- Node.js 18+ and npm
- PostgreSQL (optional, H2 used by default)
- OpenAI API key (for AI recipe generation)

### Backend Setup

1. **Build all modules:**
   ```bash
   mvn clean install
   ```

2. **Configure API (optional):**
   Edit `api/src/main/resources/application.yml`:
   - Set `OPENAI_API_KEY` environment variable for AI features
   - Configure PostgreSQL if needed
   - Adjust storage settings

3. **Run the API:**
   ```bash
   cd api
   mvn spring-boot:run
   ```
   API runs on http://localhost:8080

### Frontend Setup

1. **Install dependencies:**
   ```bash
   cd web
   npm install
   ```

2. **Configure API URL (optional):**
   Create `web/.env.local`:
   ```
   NEXT_PUBLIC_API_URL=http://localhost:8080
   ```

3. **Run the frontend:**
   ```bash
   npm run dev
   ```
   Frontend runs on http://localhost:3000

## Architecture Overview

```
┌─────────────┐
│   Web UI    │ (Next.js on :3000)
└──────┬──────┘
       │ REST API
       ↓
┌─────────────────────┐
│   API Layer         │ (Spring Boot on :8080)
│  - Controllers      │
│  - Services         │
│  - Repositories     │
└──────┬──────────────┘
       │
       ├──→ Database (PostgreSQL/H2)
       ├──→ Storage (MinIO/Local)
       ├──→ Engine (Transformation)
       └──→ RAG Service (AI Generation)
```

## Module Structure

- `engine/` - Core transformation engine (original)
- `api/` - REST API layer (Spring Boot)
- `rag-service/` - AI recipe generation service
- `web/` - Frontend dashboard (Next.js)
- `custom-actions/` - Custom transformation actions

## Environment Variables

### API (application.yml or environment)
- `OPENAI_API_KEY` - Required for AI recipe generation
- `DB_USERNAME`, `DB_PASSWORD` - PostgreSQL credentials (optional)
- `STORAGE_TYPE` - `local` or `minio` (default: `local`)

### Frontend (.env.local)
- `NEXT_PUBLIC_API_URL` - API base URL (default: http://localhost:8080)

## Testing the System

1. **Start backend:**
   ```bash
   cd api && mvn spring-boot:run
   ```

2. **Start frontend:**
   ```bash
   cd web && npm run dev
   ```

3. **Access the UI:**
   - Open http://localhost:3000
   - Create a project
   - Upload a ZIP file with Java code
   - Generate or create a recipe
   - Run a transformation job

## Key Features Implemented

✅ REST API with full CRUD operations
✅ Database persistence (H2/PostgreSQL)
✅ File storage abstraction (Local/MinIO)
✅ Async job execution
✅ AI-powered recipe generation (RAG)
✅ Web dashboard with project/recipe/job management
✅ Project file upload (ZIP)
✅ Recipe library and search
✅ Job monitoring and status tracking

## Next Steps for Enhancement

- Add authentication/authorization
- Implement real-time job updates (WebSocket)
- Add diff viewer for transformations
- Create recipe editor with Monaco
- Add analytics dashboard
- CI/CD integrations

