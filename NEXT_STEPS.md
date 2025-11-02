# Next Steps - CodeForge Development

## ✅ Working Now (MVP Complete!)

1. **Full Transformation Pipeline**
   - Upload project → Select recipes → Run job → View output logs
   - Status updates correctly (PENDING → RUNNING → COMPLETED)
   - Auto-refresh on job detail page

2. **Recipe Management**
   - Discover recipes from `resources/` folder
   - View full recipe JSON with expandable steps
   - Select multiple recipes for batch execution

3. **UI/UX**
   - Modern dark theme with gradient accents
   - Persistent navigation across all pages
   - Responsive design

## 🐛 Known Issues to Fix

### 1. Diff Viewer Not Working
**Status**: Failing during JSON serialization
**Error**: Hangs/crashes when serializing large diffs
**Impact**: "No diff available for this recipe" shown to user
**Workaround**: Output logs are still available

**Possible fixes:**
- Limit diff to only changed files
- Stream diff generation instead of holding in memory
- Paginate diff results
- Use a different serialization approach

### 2. "Unknown action: key" Error
**Status**: Recipe parsing issue in engine
**Affected Recipes**: `jaxrs-to-spring-mvc`, possibly others with `migrateAnnotation`
**Working Recipes**: `test-validation-recipe`, `ejb-to-spring-beans`, `cdi-to-spring-injection`

**Error in logs:**
```
java.lang.IllegalArgumentException: Unknown action: key
at gst.engine.actions.ActionFactory.create(ActionFactory.java:77)
```

**Investigation needed:**
- Check how `migrateAnnotation` parses `attributeMap`
- Verify JSON structure in `jaxrs-to-spring-mvc.json`
- Test with simplified recipe to isolate issue

**Location to check:**
- `engine/src/main/java/gst/engine/actions/MigrateAnnotationAction.java`
- `engine/src/main/java/gst/api/ActionSpecDeserializer.java`
- `resources/jaxrs-to-spring-mvc.json` lines 17-19

## 🎯 Priority Tasks

### High Priority
1. **Fix "Unknown action: key" error**
   - Debug `MigrateAnnotationAction` deserialization
   - Verify `attributeMap` handling
   - Test jaxrs-to-spring-mvc recipe

2. **Fix diff serialization**
   - Optimize for large file sets
   - Add streaming or chunking
   - Handle empty diffs gracefully

### Medium Priority
3. **Job Queue Management**
   - Currently: All jobs run in parallel (4-8 threads)
   - Option: Add single-threaded executor for sequential execution
   - Add queue visualization in UI

4. **Error Messaging**
   - Surface recipe errors to frontend
   - Show which recipe failed in job detail
   - Display actionable error messages

### Nice to Have
5. **Recipe Validation**
   - Validate recipe JSON before execution
   - Show validation errors in UI
   - Suggest fixes for common issues

6. **Download Transformed Code**
   - Add "Download ZIP" button for transformed output
   - Package only changed files
   - Include diff summary

## 🧪 Testing Checklist

- [x] Upload project
- [x] Select recipe
- [x] Create job
- [x] Job status updates
- [x] View logs
- [ ] View diffs (currently broken)
- [x] Multiple recipes in one job
- [x] Parallel job execution
- [ ] Recipe with actual transformations (jsf-beans, etc)
- [ ] Error handling for failed recipes

## 📝 Technical Debt

1. **Database**: Using H2 in-memory (resets on restart)
   - Consider file-based H2 or PostgreSQL
   
2. **Storage**: Local filesystem
   - Works for development
   - MinIO integration ready for production

3. **Logging**: Console only
   - Consider structured logging to file
   - Log aggregation for production

4. **Authentication**: None currently
   - User model exists but not enforced
   - Add JWT/OAuth for multi-user

## 🚀 Future Enhancements

- **Recipe Templates**: Pre-filled templates for common transformations
- **Recipe Testing**: Test recipes against sample code
- **Batch Projects**: Apply recipes to multiple projects
- **Recipe Marketplace**: Share and discover community recipes
- **AI Recipe Generation**: RAG service is ready but needs integration
- **Rollback**: Restore original code if transformation fails
- **Validation Rules**: Pre-flight checks before transformation

## 📊 Current Stack

- **Backend**: Spring Boot 3.2, Java 21, H2 Database
- **Frontend**: Next.js 14, TypeScript, Tailwind CSS
- **Engine**: JavaParser, Custom AST transformation
- **Storage**: Local filesystem (MinIO ready)
- **AI**: OpenAI integration (optional)

