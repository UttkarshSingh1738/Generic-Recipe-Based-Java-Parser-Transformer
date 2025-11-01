'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { FileCode } from 'lucide-react'
import { recipeApi } from '@/lib/api'

export default function NewRecipePage() {
  const router = useRouter()
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [recipeJson, setRecipeJson] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSaving(true)

    try {
      // Validate JSON
      JSON.parse(recipeJson)

      const response = await recipeApi.create({
        name,
        description,
        recipeJson,
        version: '1.0.0',
        author: 'user',
        category: 'custom',
        isPublic: false,
      })

      router.push(`/recipes/${response.data.id}`)
    } catch (err: any) {
      if (err instanceof SyntaxError) {
        setError('Invalid JSON format: ' + err.message)
      } else {
        setError(err.response?.data?.error || 'Failed to create recipe')
      }
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      <Link href="/recipes" className="text-gray-600 hover:text-gray-900 mb-4 inline-block">
        ← Back to Recipes
      </Link>
      <h1 className="text-3xl font-bold mb-8">Create New Recipe</h1>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="grid md:grid-cols-2 gap-6">
          <div>
            <label className="block text-sm font-medium mb-2">Recipe Name</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
              placeholder="My Recipe"
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-2">Description</label>
            <input
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
              placeholder="What does this recipe do?"
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium mb-2">Recipe JSON</label>
          <textarea
            value={recipeJson}
            onChange={(e) => setRecipeJson(e.target.value)}
            rows={20}
            required
            className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent font-mono text-sm"
            placeholder={`{
  "recipes": [
    {
      "name": "RecipeName",
      "steps": [
        {
          "match": {
            "nodeType": "ClassOrInterfaceDeclaration"
          },
          "actions": []
        }
      ]
    }
  ]
}`}
          />
          <p className="text-xs text-gray-500 mt-2">
            Enter the recipe JSON definition. See existing recipes for examples.
          </p>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        <div className="flex gap-4">
          <button
            type="submit"
            disabled={saving || !name || !recipeJson}
            className="flex-1 bg-primary text-white px-6 py-3 rounded-lg font-semibold hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {saving ? 'Saving...' : 'Save Recipe'}
          </button>
          <button
            type="button"
            onClick={() => router.back()}
            className="px-6 py-3 border rounded-lg hover:bg-gray-50"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}

