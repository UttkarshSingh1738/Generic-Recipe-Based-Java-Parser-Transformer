'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { Plus, Search, Sparkles, FileCode } from 'lucide-react'
import { recipeApi } from '@/lib/api'
import Navbar from '@/components/Navbar'

interface DiscoveredRecipe {
  fileName: string
  name: string
  description: string
  filePath: string
}

export default function RecipesPage() {
  const [discoveredRecipes, setDiscoveredRecipes] = useState<DiscoveredRecipe[]>([])
  const [dbRecipes, setDbRecipes] = useState<any[]>([])
  const [searchQuery, setSearchQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [activeTab, setActiveTab] = useState<'discovered' | 'database'>('discovered')

  useEffect(() => {
    loadRecipes()
  }, [])

  const loadRecipes = async () => {
    try {
      const [discoveredRes, dbRes] = await Promise.all([
        recipeApi.getDiscovered().catch(() => ({ data: [] })),
        recipeApi.getAll().catch(() => ({ data: [] })),
      ])
      setDiscoveredRecipes(discoveredRes.data || [])
      setDbRecipes(dbRes.data || [])
    } catch (error) {
      console.error('Failed to load recipes', error)
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = async () => {
    if (!searchQuery.trim()) {
      loadRecipes()
      return
    }

    try {
      const response = await recipeApi.search(searchQuery)
      setDbRecipes(response.data)
      setActiveTab('database')
    } catch (error) {
      console.error('Search failed', error)
    }
  }

  const filteredDiscovered = discoveredRecipes.filter(recipe =>
    recipe.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    recipe.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
    recipe.fileName.toLowerCase().includes(searchQuery.toLowerCase())
  )

  return (
    <div className="min-h-screen">
      <Navbar />
      <div className="container mx-auto px-4 py-8">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-xl font-semibold text-gray-900">Recipes</h1>
        <div className="flex gap-2">
        <Link
          href="/recipes/generate"
          className="bg-blue-600 text-white px-4 py-2 rounded text-sm font-medium flex items-center gap-2 hover:bg-blue-700"
        >
          <Sparkles className="w-4 h-4" />
          Generate (AI)
        </Link>
        <Link
          href="/recipes/new"
          className="border border-gray-300 text-gray-700 px-4 py-2 rounded text-sm font-medium flex items-center gap-2 hover:bg-gray-50"
        >
          <Plus className="w-4 h-4" />
          New recipe
        </Link>
        </div>
      </div>

      <div className="mb-4">
        <div className="flex gap-2">
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            placeholder="Search..."
            className="flex-1 px-3 py-2 border border-gray-300 rounded text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
          <button
            onClick={handleSearch}
            className="px-4 py-2 border border-gray-300 rounded text-sm font-medium hover:bg-gray-50 flex items-center gap-2"
          >
            <Search className="w-4 h-4" />
            Search
          </button>
        </div>
      </div>

      <div className="mb-4 border-b border-gray-200">
        <div className="flex gap-2">
          <button
            onClick={() => setActiveTab('discovered')}
            className={`px-3 py-2 border-b-2 text-sm font-medium ${
              activeTab === 'discovered'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            Available ({discoveredRecipes.length})
          </button>
          <button
            onClick={() => setActiveTab('database')}
            className={`px-3 py-2 border-b-2 text-sm font-medium ${
              activeTab === 'database'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            Saved ({dbRecipes.length})
          </button>
        </div>
      </div>

      {loading ? (
        <div className="text-center py-12 text-gray-500">Loading...</div>
      ) : activeTab === 'discovered' ? (
        filteredDiscovered.length === 0 ? (
          <div className="text-center py-12 border border-gray-300 rounded bg-white">
            <p className="text-gray-600 text-sm mb-2">
              {searchQuery ? 'No match' : 'No recipes in resources folder'}
            </p>
            {!searchQuery && <p className="text-xs text-gray-500">Add JSON files to resources/</p>}
          </div>
        ) : (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredDiscovered.map((recipe) => (
              <Link
                key={recipe.fileName}
                href={`/recipes/${recipe.fileName}`}
                className="border border-gray-300 rounded p-4 bg-white hover:bg-gray-50 block"
              >
                <div className="flex items-start gap-2 mb-2">
                  <FileCode className="w-5 h-5 text-gray-500 mt-0.5 flex-shrink-0" />
                  <div className="flex-1 min-w-0">
                    <h3 className="font-medium text-gray-900">{recipe.name}</h3>
                    <p className="text-xs text-gray-500 font-mono truncate">{recipe.fileName}.json</p>
                  </div>
                </div>
                <p className="text-gray-600 text-sm line-clamp-2">
                  {recipe.description || 'No description'}
                </p>
                <span className="text-xs text-gray-500 mt-2 inline-block">View details →</span>
              </Link>
            ))}
          </div>
        )
      ) : dbRecipes.length === 0 ? (
        <div className="text-center py-12 border border-gray-300 rounded bg-white">
          <p className="text-gray-600 text-sm mb-3">No saved recipes</p>
          <Link
            href="/recipes/generate"
            className="inline-block bg-blue-600 text-white px-4 py-2 rounded text-sm font-medium hover:bg-blue-700"
          >
            Generate with AI
          </Link>
        </div>
      ) : (
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
          {dbRecipes.map((recipe) => (
            <Link
              key={recipe.id}
              href={`/recipes/${recipe.id}`}
              className="border border-gray-300 rounded p-4 bg-white hover:bg-gray-50"
            >
              <div className="flex justify-between items-start mb-1">
                <h3 className="font-medium text-gray-900">{recipe.name}</h3>
                {recipe.isPublic && (
                  <span className="text-xs bg-blue-100 text-blue-800 px-2 py-0.5 rounded">Public</span>
                )}
              </div>
              <p className="text-gray-600 text-sm line-clamp-2">
                {recipe.description || 'No description'}
              </p>
              {recipe.category && (
                <span className="text-xs text-gray-500 mt-2 inline-block">{recipe.category}</span>
              )}
            </Link>
          ))}
        </div>
      )}
      </div>
    </div>
  )
}

