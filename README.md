# CodeForge - Enterprise Java Code Transformation Platform

> Transform Java codebases with AI-powered recipes. Modern, scalable, and enterprise-ready.

![CodeForge](https://img.shields.io/badge/CodeForge-Enterprise-blue?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green?style=flat-square)
![Next.js](https://img.shields.io/badge/Next.js-14-black?style=flat-square)

## ✨ Features

- **🎯 Recipe-Based Transformations**: Declarative JSON recipes for precise AST transformations
- **🤖 AI-Powered Generation**: Generate recipes from natural language using RAG (Retrieval-Augmented Generation)
- **🏢 Enterprise-Ready**: REST API, async job queuing, progress tracking, validation, and rollback capabilities
- **🎨 Modern Web Dashboard**: Beautiful dark-themed Next.js UI with gradient accents
- **☁️ Scalable Storage**: Support for local filesystem and S3-compatible storage (MinIO)
- **🔍 Recipe Discovery**: Automatically discover and manage recipes from your resources folder
- **📊 Diff & Log Analysis**: View detailed diffs and execution logs for each transformation

## 🚀 Quick Start

### Prerequisites

- **Java 21+**
- **Maven 3.8+**
- **Node.js 18+** and npm
- **(Optional)** PostgreSQL, OpenAI API key for AI features

### Backend Setup

```bash
# Build all modules
mvn clean install

# Run the Spring Boot API
cd api
mvn spring-boot:run
```

API will be available at `http://localhost:8080`

### Frontend Setup

```bash
cd web
npm install
npm run dev
```

Frontend will be available at `http://localhost:3000`

> 📘 See [SETUP.md](SETUP.md) for detailed setup and configuration instructions.

## 🏗️ Architecture

The platform consists of several modules:

- **`engine/`** - Core transformation engine using JavaParser for AST manipulation
- **`api/`** - REST API layer built with Spring Boot
- **`rag-service/`** - AI recipe generation service with OpenAI integration
- **`web/`** - Modern frontend dashboard built with Next.js 14 and TypeScript
- **`custom-actions/`** - Extensible custom transformation actions

## 📖 Documentation

- **[SETUP.md](SETUP.md)** - Detailed setup and configuration guide
- **[PROGRESS.md](PROGRESS.md)** - Implementation progress and status
- **[ARCHITECTURE_OVERVIEW.md](ARCHITECTURE_OVERVIEW.md)** - Deep dive into the engine architecture
- **`docs/`** - Recipe system documentation:
  - `nodeTypes.yml` - Supported AST node types
  - `matches.yml` - Match criteria documentation
  - `actions.yml` - Available transformation actions
  - `validators.yml` - Validation rules

## 🔧 Configuration

### API Configuration (`api/src/main/resources/application.yml`)

```yaml
# Database (H2 for development, PostgreSQL for production)
spring:
  datasource:
    url: jdbc:h2:mem:recipe_db  # Change to PostgreSQL URL for production

# Storage (local filesystem or MinIO)
storage:
  type: local  # Options: 'local' or 'minio'

# OpenAI API (for AI-powered recipe generation)
openai:
  api-key: ${OPENAI_API_KEY:}
  model: gpt-4
  embedding-model: text-embedding-3-small

# Recipe resources path
recipes:
  resources-path: ../resources
```

### Frontend Configuration (`web/.env.local`)

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## 📚 Usage Examples

### Browse and Select Recipes

1. Navigate to the **Recipes** page
2. Browse available recipes from the `resources/` folder
3. Click on any recipe to view its complete JSON structure
4. Select multiple recipes to build your transformation pipeline

### Create a Custom Recipe

```json
{
  "recipes": [{
    "name": "ConvertDateToLocalDateTime",
    "description": "Replace Date with LocalDateTime",
    "steps": [{
      "match": {
        "nodeType": "ObjectCreationExpr",
        "fqn": "java.util.Date"
      },
      "actions": [{
        "replaceWithMethodCall": {
          "scope": "LocalDateTime",
          "method": "now"
        }
      }, {
        "addImport": {
          "name": "java.time.LocalDateTime"
        }
      }]
    }]
  }]
}
```

### Run a Transformation

1. Upload your Java project (ZIP file)
2. Select one or more recipes from the Recipe Library
3. Start the transformation job
4. View detailed diffs and logs for each recipe execution

### Generate Recipe with AI

```bash
POST /api/recipes/generate
{
  "intent": "Convert all Date objects to LocalDateTime and replace new Date() with LocalDateTime.now()"
}
```

## 🛠️ Development

### Building the Project

```bash
# Build all modules
mvn clean install

# Build specific module
cd api && mvn clean install
```

### Running Tests

```bash
mvn test
```

### Frontend Development

```bash
cd web
npm install
npm run dev        # Development server
npm run build      # Production build
npm run start      # Start production server
```

### Project Structure

```
.
├── engine/              # Core transformation engine
├── api/                 # Spring Boot REST API
├── rag-service/         # AI recipe generation
├── web/                 # Next.js frontend
├── custom-actions/      # Custom transformation actions
├── resources/           # Recipe JSON files
├── docs/               # Documentation files
└── README.md           # This file
```

## 🎨 UI Features

- **Dark Theme**: Modern dark UI with gradient accents
- **Recipe Discovery**: Automatic scanning of recipe files
- **Interactive Recipe Viewer**: View full recipe JSON with expandable steps
- **Transformation Dashboard**: Track job progress and view results
- **Diff Viewer**: Side-by-side comparison of transformed code
- **Log Analysis**: View detailed execution logs for debugging

## 🔄 Workflow

1. **Upload Project**: Upload your Java project as a ZIP file
2. **Select Recipes**: Choose recipes from the library or create custom ones
3. **Run Transformation**: Start a transformation job
4. **Analyze Results**: Review diffs, logs, and transformed files
5. **Download Output**: Download the transformed codebase

## 📝 License

[Add your license here]

## 🤝 Contributing

[Add contribution guidelines here]

## 📧 Support

For issues, questions, or contributions, please [open an issue](https://github.com/yourusername/codeforge/issues).

---

**Built with ❤️ using Java, Spring Boot, and Next.js**
