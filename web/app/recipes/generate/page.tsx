'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { Sparkles, Loader2 } from 'lucide-react'
import { recipeApi } from '@/lib/api'

export default function GenerateRecipePage() {
  const router = useRouter()
  const [intent, setIntent] = useState('')
  const [generating, setGenerating] = useState(false)
  const [generatedRecipe, setGeneratedRecipe] = useState<any>(null)
  const [error, setError] = useState('')

  const handleGenerate = async () => {
    if (!intent.trim()) {
      setError('Please describe what you want to transform')
      return
    }

    setGenerating(true)
    setError('')
    setGeneratedRecipe(null)

    try {
      const response = await recipeApi.generate(intent)
      setGeneratedRecipe(response.data)
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to generate recipe')
    } finally {
      setGenerating(false)
    }
  }

  const handleSave = async () => {
    if (!generatedRecipe) return

    try {
      const response = await recipeApi.generateAndSave({
        intent,
        author: 'user',
        category: 'generated',
      })
      router.push(`/recipes/${response.data.id}`)
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to save recipe')
    }
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      <h1 className="text-3xl font-bold mb-8 flex items-center gap-2">
        <Sparkles className="w-8 h-8 text-primary" />
        Generate Recipe with AI
      </h1>

      <div className="space-y-6">
        <div>
          <label className="block text-sm font-medium mb-2">
            Describe the transformation you want
          </label>
          <textarea
            value={intent}
            onChange={(e) => setIntent(e.target.value)}
            rows={6}
            className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
            placeholder="Example: Convert all Date objects to LocalDateTime, replace new Date() with LocalDateTime.now(), and update method parameters..."
          />
          <p className="text-sm text-gray-600 mt-2">
            Be specific about what code patterns to match and what transformations to apply
          </p>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        <button
          onClick={handleGenerate}
          disabled={generating || !intent.trim()}
          className="bg-primary text-white px-6 py-3 rounded-lg font-semibold hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
        >
          {generating ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" />
              Generating...
            </>
          ) : (
            <>
              <Sparkles className="w-4 h-4" />
              Generate Recipe
            </>
          )}
        </button>

        {generatedRecipe && (
          <div className="border rounded-lg p-6 space-y-4">
            <div>
              <h3 className="text-xl font-semibold mb-2">{generatedRecipe.name}</h3>
              <p className="text-gray-600">{generatedRecipe.description}</p>
            </div>

            <div>
              <h4 className="font-semibold mb-2">Generated Recipe JSON:</h4>
              <pre className="bg-gray-50 p-4 rounded overflow-auto text-sm">
                {JSON.stringify(JSON.parse(generatedRecipe.recipeJson), null, 2)}
              </pre>
            </div>

            <div className="flex gap-4">
              <button
                onClick={handleSave}
                className="bg-primary text-white px-6 py-2 rounded-lg hover:bg-primary/90"
              >
                Save Recipe
              </button>
              <button
                onClick={() => setGeneratedRecipe(null)}
                className="border border-gray-300 px-6 py-2 rounded-lg hover:bg-gray-50"
              >
                Generate Another
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

