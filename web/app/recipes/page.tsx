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
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-white">Recipe Library</h1>
        <div className="flex gap-4">
        <Link
          href="/recipes/generate"
          className="bg-gradient-to-r from-blue-500 to-purple-600 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:from-blue-600 hover:to-purple-700 transition-all shadow-lg shadow-blue-500/25"
        >
          <Sparkles className="w-4 h-4" />
          Generate with AI
        </Link>
        <Link
          href="/recipes/new"
          className="border border-gray-700 text-gray-300 px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-gray-800 hover:border-gray-600 transition-all"
        >
          <Plus className="w-4 h-4" />
          New Recipe
        </Link>
        </div>
      </div>

      <div className="mb-6">
        <div className="flex gap-2">
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            placeholder="Search recipes..."
            className="flex-1 px-4 py-2 bg-gray-900 border border-gray-700 text-white rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent placeholder-gray-500"
          />
          <button
            onClick={handleSearch}
            className="px-6 py-2 bg-gray-800 border border-gray-700 text-gray-300 rounded-lg hover:bg-gray-700 hover:text-white transition-all flex items-center gap-2"
          >
            <Search className="w-4 h-4" />
            Search
          </button>
        </div>
      </div>

      <div className="mb-6 border-b border-gray-800">
        <div className="flex gap-4">
          <button
            onClick={() => setActiveTab('discovered')}
            className={`px-4 py-2 border-b-2 transition-colors ${
              activeTab === 'discovered'
                ? 'border-blue-500 text-blue-400 font-semibold'
                : 'border-transparent text-gray-400 hover:text-gray-300'
            }`}
          >
            Available Recipes ({discoveredRecipes.length})
          </button>
          <button
            onClick={() => setActiveTab('database')}
            className={`px-4 py-2 border-b-2 transition-colors ${
              activeTab === 'database'
                ? 'border-blue-500 text-blue-400 font-semibold'
                : 'border-transparent text-gray-400 hover:text-gray-300'
            }`}
          >
            Saved Recipes ({dbRecipes.length})
          </button>
        </div>
      </div>

      {loading ? (
        <div className="text-center py-12 text-gray-400">Loading recipes...</div>
      ) : activeTab === 'discovered' ? (
        filteredDiscovered.length === 0 ? (
          <div className="text-center py-12 border border-gray-800 rounded-lg bg-gray-900/50">
            <p className="text-gray-400 mb-4">
              {searchQuery ? 'No recipes match your search' : 'No recipes found in resources folder'}
            </p>
            {!searchQuery && (
              <p className="text-sm text-gray-500">
                Make sure recipe JSON files are in the resources folder
              </p>
            )}
          </div>
        ) : (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredDiscovered.map((recipe) => (
              <Link
                key={recipe.fileName}
                href={`/recipes/${recipe.fileName}`}
                className="border border-gray-800 rounded-lg p-6 bg-gray-900/50 hover:bg-gray-900 hover:border-gray-700 transition-all cursor-pointer block"
              >
                <div className="flex items-start gap-3 mb-3">
                  <FileCode className="w-6 h-6 text-blue-400 mt-0.5 flex-shrink-0" />
                  <div className="flex-1 min-w-0">
                    <h3 className="text-xl font-semibold mb-1 text-white">{recipe.name}</h3>
                    <p className="text-xs text-gray-500 font-mono truncate">{recipe.fileName}.json</p>
                  </div>
                </div>
                <p className="text-gray-400 text-sm mb-4 line-clamp-3">
                  {recipe.description || 'No description'}
                </p>
                <div className="flex items-center gap-2">
                  <div className="text-xs bg-green-900/50 text-green-400 border border-green-800 px-2 py-1 rounded font-medium">
                    ✓ Ready to use
                  </div>
                  <div className="text-xs text-gray-500">Click to view details</div>
                </div>
              </Link>
            ))}
          </div>
        )
      ) : dbRecipes.length === 0 ? (
        <div className="text-center py-12 border border-gray-800 rounded-lg bg-gray-900/50">
          <p className="text-gray-400 mb-4">No saved recipes found</p>
          <Link
            href="/recipes/generate"
            className="inline-block bg-gradient-to-r from-blue-500 to-purple-600 text-white px-4 py-2 rounded-lg hover:from-blue-600 hover:to-purple-700 transition-all shadow-lg shadow-blue-500/25"
          >
            Generate Your First Recipe
          </Link>
        </div>
      ) : (
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {dbRecipes.map((recipe) => (
            <Link
              key={recipe.id}
              href={`/recipes/${recipe.id}`}
              className="border border-gray-800 rounded-lg p-6 bg-gray-900/50 hover:bg-gray-900 hover:border-gray-700 transition-all"
            >
              <div className="flex justify-between items-start mb-2">
                <h3 className="text-xl font-semibold text-white">{recipe.name}</h3>
                {recipe.isPublic && (
                  <span className="text-xs bg-blue-900/50 text-blue-400 border border-blue-800 px-2 py-1 rounded">
                    Public
                  </span>
                )}
              </div>
              <p className="text-gray-400 text-sm mb-4 line-clamp-2">
                {recipe.description || 'No description'}
              </p>
              <div className="flex items-center gap-4 text-sm text-gray-500">
                {recipe.category && (
                  <span className="bg-gray-800 border border-gray-700 px-2 py-1 rounded text-gray-400">{recipe.category}</span>
                )}
                {recipe.author && <span className="text-gray-500">by {recipe.author}</span>}
              </div>
            </Link>
          ))}
        </div>
      )}
      </div>
    </div>
  )
}

