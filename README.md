# Generic Recipe-Based Java Parser Transformer

Automated Java code transformation engine with declarative JSON recipes. Built for framework migrations, modernization, and large-scale refactoring.

---

## Features

- **Declarative Recipes** - JSON-based transformation rules
- **37 Built-in Actions** - Comprehensive AST manipulation
- **Symbol Resolution** - Type-aware transformations
- **Web Interface** - Modern UI for project management
- **Extensible** - Custom actions via SPI
- **Battle-tested** - JavaParser foundation with validation

---

## Quick Start

```bash
# Build
mvn clean install -DskipTests

# Start API
cd api && mvn spring-boot:run

# Start Frontend (new terminal)
cd web && npm install && npm run dev
```

**API**: http://localhost:8080  
**UI**: http://localhost:3000

---

## Usage

### Web Interface

1. Upload Java project
2. Select transformation recipes
3. Run transformation
4. Review diffs and download

### Command Line

```bash
# Configure recipes in config.json
["jaxrs-to-spring-mvc", "cdi-to-spring-injection"]

# Run
java -cp "engine/target/engine-1.0-SNAPSHOT-shaded.jar" gst.Main <input-dir>
```

---

## Recipe Example

```json
{
  "recipes": [{
    "name": "JaxRsToSpringMvc",
    "description": "Convert JAX-RS to Spring MVC",
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

---

## Available Recipes

**Framework Migrations** (16 recipes):
- JAX-RS → Spring MVC
- CDI → Spring DI
- EJB → Spring Beans
- JSF → Spring Components

**Java Modernization**:
- Java 8-11 → 17 (pattern matching, switch expressions)
- String formatting
- Enhanced for loops

See `resources/` for all recipes.

---

## Architecture

```
engine/           JavaParser-based transformation core
api/              Spring Boot REST API
web/              Next.js frontend
custom-actions/   SPI-based extensions
resources/        Recipe library
docs/             API documentation (matches, actions, validators)
```

---

## Documentation

- **Matches**: `docs/matches.yml` - 30+ match criteria
- **Actions**: `docs/actions.yml` - All transformation actions  
- **Validators**: `docs/validators.yml` - Validation rules
- **Setup**: `SETUP.md` - Detailed configuration guide

---

## Database

**Development**: H2 in-memory (auto-configured)  
**Production**: PostgreSQL (configure in `application-prod.yml`)

---

## Requirements

- Java 21+
- Maven 3.8+
- Node.js 18+

---

## License

[Your License]

---

**Built with JavaParser, Spring Boot, and Next.js**
