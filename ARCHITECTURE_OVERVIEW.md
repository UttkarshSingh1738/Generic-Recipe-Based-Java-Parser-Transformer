# Architecture Overview - What We've Built

## ✅ Yes, It's Fully Connected to Your Engine!

The API **IS** connected to your original transformation engine. Here's the complete flow:

```
Frontend (Next.js) 
    ↓ HTTP REST API
API Layer (Spring Boot)
    ↓ Service Calls
EnhancedPipelineService
    ↓ Direct Call
gst.engine.Pipeline.run() ← YOUR ORIGINAL ENGINE
```

---

## 🔗 Complete Connection Chain

### 1. **API Endpoint** → **Job Execution**
```
POST /api/jobs
  → JobController.createJob()
  → TransformationService.createJob()
  → JobExecutionService.executeJob() [ASYNC]
```

### 2. **Job Execution** → **Engine Integration**
```java
JobExecutionService.executeJob()
  ↓
  // Downloads project files from storage
  // Loads recipes from database
  ↓
  EnhancedPipelineService.executeTransformation()
    ↓
    // Creates temp recipe JSON file
    ↓
    Pipeline.run(tempRecipeFile, inputPath, outputPath, ...)
      ↑
      YOUR ORIGINAL ENGINE (gst.engine.Pipeline)
```

### 3. **Engine Execution** (Your Original Code)
- Uses `gst.engine.Pipeline.run()` - your existing engine
- Processes Java files with JavaParser
- Applies recipes using `NodeMatcher` and `ActionFactory`
- Handles validation and rollback via `TxContext`
- Returns transformed files

### 4. **Results Upload**
```
After Pipeline.run() completes:
  ↓
Upload transformed files to storage
  ↓
Update job status (COMPLETED/FAILED)
  ↓
Store output paths in database
```

---

## 📦 What We've Built

### **Phase 1: Foundation** ✅
1. **REST API Layer** (`api/` module)
   - Full CRUD for Recipes, Projects, Jobs
   - Recipe generation endpoints
   - Project file upload (ZIP support)
   - Health check endpoint

2. **Database Layer**
   - PostgreSQL/H2 support
   - JPA entities: Recipe, Project, TransformationJob, User
   - Repositories with custom queries
   - Automatic schema generation

3. **File Storage Service**
   - Abstract storage interface
   - Local filesystem (dev)
   - MinIO/S3 (production)
   - Handles project uploads and transformation outputs

4. **Engine Integration** ✅ **CONNECTED**
   - `EnhancedPipelineService` wraps your `Pipeline.run()`
   - Async job execution
   - Progress tracking callbacks
   - Error handling and logging

### **Phase 2: AI/RAG System** ✅
1. **Documentation Parser**
   - Parses your YAML docs (`nodeTypes.yml`, `matches.yml`, `actions.yml`)
   - Creates document chunks for retrieval

2. **Embedding Service**
   - OpenAI integration for embeddings
   - Vector similarity search

3. **Vector Store**
   - In-memory implementation (extensible to Chroma/Weaviate)
   - Semantic search for recipe generation

4. **Recipe Generation**
   - RAG-powered LLM integration
   - Generates recipe JSON from natural language
   - Validates and saves recipes

### **Phase 3: Web Dashboard** ✅
1. **Next.js Frontend**
   - TypeScript + Tailwind CSS
   - Project management pages
   - Recipe library with search
   - Job monitoring dashboard
   - AI recipe generation UI

---

## 🔄 Complete Request Flow Example

### Creating and Running a Transformation:

```
1. User uploads project (ZIP)
   POST /api/projects/{id}/upload
   → Files stored in StorageService
   → Project metadata saved to database

2. User creates/selects recipes
   GET /api/recipes
   → Recipes loaded from database

3. User creates transformation job
   POST /api/jobs
   {
     "projectId": 1,
     "recipeNames": ["ejb-to-spring-beans"]
   }
   ↓
   JobExecutionService.executeJob() [ASYNC]
   ↓
   Downloads files from storage → temp directory
   Loads recipe JSON from database → parses to gst.api.Recipe
   ↓
   EnhancedPipelineService.executeTransformation()
     ↓
     Creates temp recipe JSON file
     ↓
     Pipeline.run(tempRecipeFile, inputPath, outputPath)
       ↑
       YOUR ENGINE PROCESSES FILES HERE
       - Matches AST nodes
       - Applies actions
       - Validates and rollbacks if needed
       - Writes transformed files
     ↓
   Uploads transformed files to storage
   Updates job status: COMPLETED
   ↓
4. User checks job status
   GET /api/jobs/{id}
   → Returns status, files transformed, output paths
```

---

## 🎯 Key Integration Points

### Engine Module (`engine/`)
- **Fully integrated** via Maven dependency
- `gst.engine.Pipeline` - your core transformation logic
- `gst.api.Recipe`, `gst.api.Match`, `gst.api.Step` - recipe models
- All actions, matchers, validators from your engine

### Your Original CLI Still Works!
The original CLI (`gst.Main`) remains untouched and works exactly as before:
```bash
java -cp "engine/target/engine-1.0-SNAPSHOT-shaded.jar" gst.Main resources/input/sample-app
```

Now you ALSO have:
- Web API for programmatic access
- Database persistence
- File storage abstraction
- Job queue management
- Web UI dashboard

---

## 📊 What Works Right Now

✅ **Fully Functional:**
- Project upload and management
- Recipe CRUD operations
- Transformation job creation and execution
- Engine integration (calls your Pipeline.run())
- Database persistence
- File storage
- Web dashboard UI

⚠️ **Optional Features (work when configured):**
- AI recipe generation (requires OpenAI API key)
- RAG system (requires docs path + OpenAI key)

---

## 🧪 Test the Engine Connection

You can test that the engine is connected by:

1. **Via API:**
   ```bash
   # Create a project
   POST /api/projects
   
   # Upload Java source code
   POST /api/projects/{id}/upload
   
   # Create a recipe (or use existing)
   POST /api/recipes
   
   # Run transformation
   POST /api/jobs
   {
     "projectId": 1,
     "recipeNames": ["ejb-to-spring-beans"]
   }
   ```

2. **Check the logs:**
   When a job runs, you'll see:
   ```
   [INFO] Starting transformation of X Java files
   [INFO] Applying recipe: ejb-to-spring-beans
   [MATCH] ClassOrInterfaceDeclaration at ...
   [ACTION] migrateAnnotation on node at ...
   [INFO] Transformation completed: X files processed, Y transformed
   ```
   These are from **YOUR ORIGINAL ENGINE** (`gst.engine.Pipeline`)

---

## 🚀 What's Next

The foundation is solid and the engine is connected. Remaining enhancements:
- Real-time progress updates (WebSocket/SSE)
- Advanced diff viewer
- Recipe editor with Monaco
- Authentication/authorization
- Analytics dashboard

**Bottom line:** Your original engine is the core, and we've built a complete enterprise platform around it without changing how it works!

