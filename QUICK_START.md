# Quick Start Guide

## ✅ Your Backend is Running Successfully!

The backend has started and is waiting for HTTP requests on **port 8080**. It's not stuck - it's working normally!

You can verify it's working by:

### 1. Test the Health Endpoint
Open in your browser or use curl:
```
http://localhost:8080/api/health
```

### 2. Test the Recipes API
```
http://localhost:8080/api/recipes
```

Should return: `[]` (empty array - no recipes yet, which is expected)

### 3. Access H2 Database Console
```
http://localhost:8080/h2-console
```
- JDBC URL: `jdbc:h2:mem:recipe_db`
- Username: `sa`
- Password: (leave empty)

---

## 🎯 Next Steps

### Frontend is Ready
Your Next.js frontend should be running on `http://localhost:3000`

### Test the Full Workflow

1. **Create a Project:**
   - Go to http://localhost:3000/projects/new
   - Upload a ZIP file with Java source code

2. **Create a Recipe:**
   - Go to http://localhost:3000/recipes/new
   - Or use one of the existing recipes from `resources/`

3. **Run a Transformation:**
   - Go to your project page
   - Create a transformation job
   - Monitor its progress

---

## ⚠️ Notes

### AI Features (Optional)
The warnings about OpenAI API key are normal. AI recipe generation will only work if you:
- Set environment variable: `OPENAI_API_KEY=your-key`
- Restart the backend

Without it, the app works fine - you just manually create recipes instead of generating them with AI.

### Documentation Path
The warning about docs directory is also normal. The RAG system will work once you configure the path correctly (currently looking for `../docs` relative to the API module).

---

## 🧪 Quick API Tests

Using PowerShell:

```powershell
# Health check
Invoke-RestMethod http://localhost:8080/api/health

# List recipes
Invoke-RestMethod http://localhost:8080/api/recipes

# List projects
Invoke-RestMethod http://localhost:8080/api/projects

# List jobs
Invoke-RestMethod http://localhost:8080/api/jobs
```

Your backend is **ready to use**! 🚀

