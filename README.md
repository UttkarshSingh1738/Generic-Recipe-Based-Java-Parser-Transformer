# Generic Recipe-Based Java Parser Transformer

> **Enterprise Java transformation platform with custom recipe DSL**

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-14-black?style=flat-square&logo=next.js)](https://nextjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-blue?style=flat-square&logo=typescript)](https://www.typescriptlang.org/)

Automated Java code transformation at scale using declarative JSON recipes. Built on JavaParser with full symbol resolution for type-aware transformations.

**[🎯 Project Highlights](PROJECT_HIGHLIGHTS.md)** · **[🔬 Technical Deep Dive](TECHNICAL_ACHIEVEMENTS.md)** · **[📖 Documentation](docs/)**

---

## Overview

Transform Java codebases with **precision** and **scale**:
- **Framework Migrations** - JAX-RS → Spring MVC, EJB → Spring, CDI → Spring DI
- **Version Upgrades** - Java 8 → 11 → 17 (pattern matching, switch expressions, records)
- **Code Refactoring** - Methods, types, annotations across entire codebases
- **Custom Transformations** - Extensible via SPI

All powered by **declarative JSON recipes** with validation and rollback.

---

## Quick Start

```bash
# Build
mvn clean install -DskipTests

# Start backend
cd api && mvn spring-boot:run

# Start frontend (new terminal)
cd web && npm install && npm run dev
```

**API**: http://localhost:8080  
**UI**: http://localhost:3000

---

## Architecture

### Components

```
┌─────────────────────────────────────────────────────┐
│ Next.js Frontend (TypeScript + Tailwind)            │
│ • Recipe selection • Job tracking • Diff viewer     │
└────────────────┬────────────────────────────────────┘
                 │ REST API
┌────────────────┴────────────────────────────────────┐
│ Spring Boot API                                     │
│ • Async jobs • Storage • Progress tracking          │
└────────────────┬────────────────────────────────────┘
                 │
    ┌────────────┼────────────┐
    │            │            │
┌───┴────┐  ┌───┴─────┐  ┌──┴──────┐
│ Engine │  │ Custom  │  │ RAG     │
│ Core   │  │ Actions │  │ Service │
└────────┘  └─────────┘  └─────────┘
```

### Technology Stack

- **Engine**: Java 21, JavaParser 3.27, Jackson
- **API**: Spring Boot 3.2, JPA/Hibernate, PostgreSQL/H2
- **Frontend**: Next.js 14, TypeScript, Tailwind CSS
- **AI**: OpenAI GPT-4, vector embeddings

---

## Recipe Format

### Example: JAX-RS to Spring MVC

```json
{
  "recipes": [{
    "name": "JaxRsToSpringMvc",
    "description": "Convert JAX-RS REST APIs to Spring MVC",
    "steps": [{
      "match": {
        "nodeType": "ClassOrInterfaceDeclaration",
        "annotation": "Path"
      },
      "actions": [{
        "migrateAnnotation": {
          "from": "Path",
          "to": "RequestMapping",
          "attributeMap": {"value": "value"}
        }
      }, {
        "addAnnotation": {"name": "RestController"}
      }]
    }]
  }]
}
```

**30+ match criteria** × **37 actions** = **Thousands of transformation combinations**

---

## Features

### Core Engine
- ✅ **37 Built-in Actions** - Comprehensive transformation library
- ✅ **Type-Aware Matching** - Full symbol resolution via JavaParser
- ✅ **Transaction Context** - Granular rollback per recipe
- ✅ **Validation Framework** - 5 validators for correctness
- ✅ **Extensible** - Custom actions via SPI

### Web Platform
- ✅ **Modern UI** - Next.js with TypeScript
- ✅ **Job Queue** - Sequential execution with progress tracking
- ✅ **Diff Viewer** - Side-by-side code comparison
- ✅ **Log Analysis** - Color-coded transformation events
- ✅ **Recipe Library** - 16 production-ready recipes

### Advanced Features
- ✅ **AI Generation** - Generate recipes from natural language (RAG)
- ✅ **Storage** - Local filesystem or S3-compatible (MinIO)
- ✅ **Database** - H2 (dev) / PostgreSQL (prod)
- ✅ **Parse Validation** - Pre/post transformation checks

---

## Available Transformations

**16 Production Recipes** covering:

| Migration Path | Recipes | Description |
|----------------|---------|-------------|
| **Java EE → Spring** | 7 | JAX-RS, CDI, EJB, JSF migrations |
| **Java 8 → 17** | 4 | Pattern matching, switch expressions, enhanced for |
| **Modernization** | 3 | String formatting, method refactoring |
| **Custom** | 2 | Domain-specific transformations |

See `resources/` for all recipes.

---

## Usage

### Via Web Interface

1. **Upload** - Drop Java project (ZIP)
2. **Select** - Choose transformation recipes
3. **Transform** - Start job, track progress
4. **Review** - View diffs, analyze logs
5. **Download** - Get transformed codebase

### Via Command Line

```bash
# Configure recipes
echo '["jaxrs-to-spring-mvc", "cdi-to-spring-injection"]' > config.json

# Run
java -cp "engine/target/engine-1.0-SNAPSHOT-shaded.jar" \
  gst.Main resources/input/my-project
```

---

## Documentation

**API Reference**:
- [`docs/matches.yml`](docs/matches.yml) - 30+ match criteria
- [`docs/actions.yml`](docs/actions.yml) - 37 transformation actions
- [`docs/validators.yml`](docs/validators.yml) - Validation rules

**Guides**:
- [`SETUP.md`](SETUP.md) - Detailed setup instructions
- [`ARCHITECTURE_OVERVIEW.md`](ARCHITECTURE_OVERVIEW.md) - System architecture
- [`PROJECT_HIGHLIGHTS.md`](PROJECT_HIGHLIGHTS.md) - Quick overview
- [`TECHNICAL_ACHIEVEMENTS.md`](TECHNICAL_ACHIEVEMENTS.md) - Technical deep dive

---

## Showcase: Real-World Example

**Before** (Java EE):
```java
@Path("/users")
@Stateless
public class UserResource {
    @EJB UserService service;
    
    @GET @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers() {
        return Response.ok(service.findAll()).build();
    }
}
```

**After** (Spring Boot) - **Fully automated**:
```java
@RestController
@RequestMapping("/users")
public class UserResource {
    @Autowired UserService service;
    
    @GetMapping(produces = MediaType.APPLICATION_JSON)
    public ResponseEntity getUsers() {
        return ResponseEntity.ok(service.findAll());
    }
}
```

**Changes**: Annotations, imports, types, return values - all handled automatically.

---

## Technical Highlights

### Hand-Crafted Components

| Component | Lines of Code | Complexity |
|-----------|---------------|------------|
| **NodeMatcher** | ~700 | Type resolution, 30+ criteria |
| **37 Actions** | ~3,000 | Edge-case handling, validation |
| **Pipeline** | ~300 | Orchestration, rollback |
| **Transaction Context** | ~150 | Granular change tracking |
| **Job Execution** | ~400 | Async, queuing, callbacks |
| **Recipe Discovery** | ~200 | Auto-scanning, metadata |

### Innovation

- **Custom DSL** - Declarative transformation language
- **Symbol Resolution** - Type-aware matching and validation
- **Granular Rollback** - Per-recipe transaction management
- **AI Integration** - RAG-powered recipe generation

---

## Development

### Project Structure
```
engine/              # Transformation engine (5K LOC)
  ├── actions/       # 37 transformation implementations
  ├── matcher/       # Type-aware node matching
  ├── validator/     # 5 validation rules
  └── Pipeline.java  # Main orchestrator

api/                 # Spring Boot REST API (3K LOC)
  ├── controllers/   # 9 REST endpoints
  ├── services/      # Business logic
  ├── models/        # JPA entities
  └── repositories/  # Data access

web/                 # Next.js frontend (2K LOC)
  ├── app/           # Pages (projects, jobs, recipes)
  ├── components/    # React components
  └── lib/           # API client

custom-actions/      # SPI examples
rag-service/         # AI recipe generation
resources/           # 16 production recipes
docs/                # API documentation
```

### Tech Stack
**Backend**: Java 21, Spring Boot 3.2, JPA, PostgreSQL  
**Frontend**: Next.js 14, TypeScript 5, Tailwind CSS  
**Engine**: JavaParser 3.27, Jackson 2.15  
**AI**: OpenAI GPT-4, vector embeddings

---

## Requirements

- Java 21+
- Maven 3.8+
- Node.js 18+
- (Optional) PostgreSQL 14+, OpenAI API key

---

## Testing

```bash
# Test engine
mvn test

# Test on sample code
java -cp "engine/target/engine-1.0-SNAPSHOT-shaded.jar" \
  gst.Main resources/input/sample-app
```

---

## License

[Your License]

---

## Links

- **[Quick Highlights](PROJECT_HIGHLIGHTS.md)** - For recruiters (3 min)
- **[Technical Details](TECHNICAL_ACHIEVEMENTS.md)** - For engineers (10 min)  
- **[Setup Guide](SETUP.md)** - Get it running
- **[Architecture](ARCHITECTURE_OVERVIEW.md)** - System design

---

**Built to demonstrate advanced software engineering: DSL design, AST manipulation, full-stack development, and production-grade architecture.**
