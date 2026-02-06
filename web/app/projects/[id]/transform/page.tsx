'use client'

import { useEffect, useState } from 'react'
import { useParams, useRouter, useSearchParams } from 'next/navigation'
import Link from 'next/link'
import { Loader2, CheckCircle2, Eye, X } from 'lucide-react'
import { projectApi, recipeApi, jobApi } from '@/lib/api'
import Navbar from '@/components/Navbar'

interface DiscoveredRecipe {
  fileName: string
  name: string
  description: string
}

export default function TransformProjectPage() {
  const params = useParams()
  const router = useRouter()
  const searchParams = useSearchParams()
  const projectId = Number(params.id)
  const [project, setProject] = useState<any>(null)
  const [recipes, setRecipes] = useState<DiscoveredRecipe[]>([])
  const [selectedRecipes, setSelectedRecipes] = useState<Set<string>>(new Set())
  const [viewingRecipe, setViewingRecipe] = useState<string | null>(null)
  const [recipeContent, setRecipeContent] = useState<string>('')
  const [loading, setLoading] = useState(true)
  const [creating, setCreating] = useState(false)
  const [loadingRecipeContent, setLoadingRecipeContent] = useState(false)

  useEffect(() => {
    if (projectId) {
      loadData()
      
      // Check if a recipe was pre-selected from URL
      const selectRecipe = searchParams?.get('selectRecipe')
      if (selectRecipe) {
        setSelectedRecipes(new Set([selectRecipe]))
      }
    }
  }, [projectId, searchParams])

  const loadData = async () => {
    try {
      const [projectRes, recipesRes] = await Promise.all([
        projectApi.getById(projectId),
        recipeApi.getDiscovered(),
      ])
      setProject(projectRes.data)
      setRecipes(recipesRes.data)
    } catch (error) {
      console.error('Failed to load data', error)
    } finally {
      setLoading(false)
    }
  }

  const toggleRecipe = (recipeName: string) => {
    const newSelected = new Set(selectedRecipes)
    if (newSelected.has(recipeName)) {
      newSelected.delete(recipeName)
    } else {
      newSelected.add(recipeName)
    }
    setSelectedRecipes(newSelected)
  }

  const viewRecipe = async (recipeName: string) => {
    setViewingRecipe(recipeName)
    setLoadingRecipeContent(true)
    try {
      const response = await recipeApi.getDiscoveredContent(recipeName)
      // Response might be text/plain, so handle both
      const content = typeof response.data === 'string' 
        ? response.data 
        : JSON.stringify(response.data, null, 2)
      setRecipeContent(content)
    } catch (error) {
      console.error('Failed to load recipe content', error)
      setRecipeContent('{}')
    } finally {
      setLoadingRecipeContent(false)
    }
  }

  const handleCreateJob = async () => {
    if (selectedRecipes.size === 0) {
      alert('Please select at least one recipe')
      return
    }

    setCreating(true)
    try {
      const response = await jobApi.create({
        projectId,
        recipeNames: Array.from(selectedRecipes),
        matchDebug: false,
      })
      router.push(`/jobs/${response.data.id}`)
    } catch (error: any) {
      console.error('Failed to create job', error)
      alert(error.response?.data?.message || 'Failed to create transformation job')
    } finally {
      setCreating(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen">
        <Navbar />
        <div className="container mx-auto px-4 py-8 flex justify-center py-12">
          <Loader2 className="w-6 h-6 animate-spin text-gray-500" />
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen">
      <Navbar />
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      <div className="mb-6">
        <Link href={`/projects/${projectId}`} className="text-gray-600 hover:text-gray-900 text-sm mb-2 inline-block">
          ← Project
        </Link>
        <h1 className="text-xl font-semibold mb-1 text-gray-900">Run transformation</h1>
        <p className="text-gray-600">
          Select one or more recipes to apply to <strong>{project?.name}</strong>
        </p>
      </div>

      <div className="mb-6">
        <h2 className="text-xl font-semibold mb-4">Available Recipes</h2>
        {recipes.length === 0 ? (
          <div className="border rounded-lg p-8 text-center text-gray-600">
            No recipes found. Make sure recipes are available in the resources folder.
          </div>
        ) : (
          <div className="grid gap-3 mb-6">
            {recipes.map((recipe) => (
              <label
                key={recipe.fileName}
                className={`border rounded p-4 cursor-pointer transition-colors ${
                  selectedRecipes.has(recipe.fileName) ? 'border-blue-500 bg-blue-50' : 'border-gray-300 hover:border-gray-400'
                }`}
              >
                <div className="flex items-start gap-3">
                  <input
                    type="checkbox"
                    checked={selectedRecipes.has(recipe.fileName)}
                    onChange={() => toggleRecipe(recipe.fileName)}
                    className="mt-1"
                  />
                  <div className="flex-1">
                    <div className="font-semibold text-lg">{recipe.name}</div>
                    <div className="text-sm text-gray-600 mt-1">{recipe.description}</div>
                    <div className="text-xs text-gray-500 mt-2 font-mono bg-gray-50 px-2 py-1 rounded inline-block">
                      {recipe.fileName}.json
                    </div>
                  </div>
                  <div className="flex items-center gap-2 flex-shrink-0">
                    <button
                      onClick={(e) => {
                        e.stopPropagation()
                        viewRecipe(recipe.fileName)
                      }}
                      className="p-1.5 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded"
                      title="View recipe details"
                    >
                      <Eye className="w-4 h-4" />
                    </button>
                    {selectedRecipes.has(recipe.fileName) && (
                      <CheckCircle2 className="w-5 h-5 text-blue-600" />
                    )}
                  </div>
                </div>
              </label>
            ))}
          </div>
        )}
      </div>

      {/* Selected Recipes Summary */}
      {selectedRecipes.size > 0 && (
        <div className="mb-6 border border-blue-200 rounded-lg p-4 bg-blue-50">
          <h3 className="font-semibold mb-3 text-gray-900">
            Selected Recipes ({selectedRecipes.size}):
          </h3>
          <div className="flex flex-wrap gap-2">
            {Array.from(selectedRecipes).map((recipeName) => {
              const recipe = recipes.find(r => r.fileName === recipeName)
              return (
                <div
                  key={recipeName}
                  className="flex items-center gap-2 bg-white px-3 py-2 rounded-md border border-gray-300 shadow-sm"
                >
                  <span className="text-sm font-medium text-gray-900">{recipe?.name || recipeName}</span>
                  <button
                    onClick={() => toggleRecipe(recipeName)}
                    className="text-gray-500 hover:text-red-600 hover:bg-red-50 p-0.5 rounded transition-colors"
                    title="Remove"
                  >
                    <X className="w-4 h-4" />
                  </button>
                </div>
              )
            })}
          </div>
        </div>
      )}

      {/* Recipe Viewer Modal */}
      {viewingRecipe && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col">
            <div className="px-6 py-4 border-b flex items-center justify-between">
              <h2 className="text-xl font-semibold">
                {recipes.find(r => r.fileName === viewingRecipe)?.name || viewingRecipe}
              </h2>
              <button
                onClick={() => {
                  setViewingRecipe(null)
                  setRecipeContent('')
                }}
                className="text-gray-500 hover:text-gray-900"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="flex-1 overflow-auto p-6">
              {loadingRecipeContent ? (
                <div className="text-center py-12">Loading recipe...</div>
              ) : (
                <div className="space-y-4">
                  <div>
                    <button
                      onClick={() => {
                        toggleRecipe(viewingRecipe)
                      }}
                      className={`px-4 py-2 rounded text-sm font-medium flex items-center gap-2 ${
                        selectedRecipes.has(viewingRecipe)
                          ? 'bg-green-100 text-green-800'
                          : 'bg-blue-600 text-white hover:bg-blue-700'
                      }`}
                    >
                      {selectedRecipes.has(viewingRecipe) ? (
                        <>
                          <CheckCircle2 className="w-4 h-4" />
                          Selected - Click to Remove
                        </>
                      ) : (
                        <>
                          <CheckCircle2 className="w-4 h-4" />
                          Add to Selection
                        </>
                      )}
                    </button>
                  </div>
                  <div className="border rounded-lg overflow-hidden">
                    <div className="bg-gray-50 px-4 py-2 border-b">
                      <span className="text-sm font-semibold">Full Recipe JSON</span>
                    </div>
                    <pre className="p-4 bg-gray-100 text-gray-800 text-xs overflow-x-auto max-h-[600px] overflow-y-auto font-mono whitespace-pre-wrap break-words border-t border-gray-200">
                      {(() => {
                        try {
                          const parsed = typeof recipeContent === 'string' 
                            ? JSON.parse(recipeContent) 
                            : recipeContent
                          return JSON.stringify(parsed, null, 2)
                        } catch (e) {
                          return recipeContent || '{}'
                        }
                      })()}
                    </pre>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      <div className="flex gap-3">
        <Link
          href={`/projects/${projectId}`}
          className="px-4 py-2 border border-gray-300 rounded text-sm hover:bg-gray-50"
        >
          Cancel
        </Link>
        <button
          onClick={handleCreateJob}
          disabled={selectedRecipes.size === 0 || creating}
          className="px-4 py-2 bg-blue-600 text-white rounded text-sm font-medium hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2"
        >
          {creating ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" />
              Creating Job...
            </>
          ) : (
            <>
              Run Transformation ({selectedRecipes.size} recipe{selectedRecipes.size !== 1 ? 's' : ''})
            </>
          )}
        </button>
      </div>
    </div>
    </div>
  )
}

