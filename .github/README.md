# 🌟 Quick Links for Recruiters

**Reviewing this repository? Start here!**

---

## 📄 Essential Reading

1. **[README.md](../README.md)** - Project overview and technical details (5 min read)
2. **[PROJECT_HIGHLIGHTS.md](../PROJECT_HIGHLIGHTS.md)** - At-a-glance achievements (3 min read)
3. **[TECHNICAL_ACHIEVEMENTS.md](../TECHNICAL_ACHIEVEMENTS.md)** - Deep technical dive (10 min read)

---

## 🎯 What To Look At

### **To See Code Quality**
- `engine/src/main/java/gst/engine/matcher/NodeMatcher.java` - 700 lines of hand-crafted type-safe matching
- `engine/src/main/java/gst/engine/actions/` - 37 transformation actions
- `api/src/main/java/com/recipe/api/services/` - Job orchestration, business logic

### **To See Architecture**
- `engine/src/main/java/gst/engine/Pipeline.java` - Main orchestration
- `engine/src/main/java/gst/engine/TxContext.java` - Transaction management
- `api/src/main/java/com/recipe/api/` - REST API structure

### **To See Full-Stack Skills**
- `web/app/` - Next.js pages and routing
- `web/components/` - React components (DiffViewer, LogViewer)
- Database schema: `api/src/main/resources/db/migration/V1__Initial_Schema.sql`

### **To See Innovation**
- `resources/*.json` - 16 hand-written transformation recipes
- `docs/*.yml` - Custom API documentation
- `engine/src/main/java/gst/api/ActionSpecDeserializer.java` - Custom Jackson deserializer

---

## 💡 Quick Understanding

**What It Does**: Automatically transforms Java code using JSON recipes  
**Scale**: 37 actions × 30+ matchers = 1000+ transformation possibilities  
**Tested On**: Enterprise codebases with 500+ classes  
**Tech Stack**: Java 21, Spring Boot, Next.js, PostgreSQL, AI (OpenAI)

---

## 📊 Project Scale

- **~15,000 lines** of production code
- **176+ files** across 5 modules
- **Full stack**: Backend (Java/Spring) + Frontend (Next.js/TypeScript) + AI (RAG)
- **Months of work**: Architecture, implementation, testing, polish

---

## 🏆 Key Strengths

1. **Original Design** - Custom DSL, not copying existing tools
2. **Production Quality** - Error handling, validation, rollback
3. **Full Ownership** - Every line designed and implemented
4. **Modern Stack** - Latest Java, Spring Boot, Next.js
5. **Enterprise Features** - Async jobs, storage, validation, AI

---

**Questions? See the main README or technical documentation!**

