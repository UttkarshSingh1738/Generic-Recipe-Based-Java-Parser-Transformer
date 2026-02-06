'use client'

import { useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import Link from 'next/link'
import { ArrowLeft, CheckCircle2, Copy, Eye, EyeOff } from 'lucide-react'
import { recipeApi } from '@/lib/api'
import Navbar from '@/components/Navbar'

export default function RecipeDetailPage() {
  const params = useParams()
  const router = useRouter()
  const recipeName = params.name as string
  const [recipe, setRecipe] = useState<any>(null)
  const [recipeContent, setRecipeContent] = useState<string>('')
  const [loading, setLoading] = useState(true)
  const [expanded, setExpanded] = useState<Set<string>>(new Set())
  const [showRawJson, setShowRawJson] = useState(false)

  useEffect(() => {
    if (recipeName) {
      loadRecipe()
    }
  }, [recipeName])

  const loadRecipe = async () => {
    setLoading(true)
    try {
      const [recipeRes, contentRes] = await Promise.all([
        recipeApi.getDiscoveredByName(recipeName),
        recipeApi.getDiscoveredContent(recipeName).catch(err => {
          console.error('Failed to load recipe content:', err)
          return { data: '{}' }
        }),
      ])
      
      if (recipeRes.data) {
        setRecipe(recipeRes.data)
      }
      
      // Handle both string and object responses
      const content = typeof contentRes.data === 'string' 
        ? contentRes.data 
        : JSON.stringify(contentRes.data, null, 2)
      setRecipeContent(content || '{}')
    } catch (error) {
      console.error('Failed to load recipe', error)
      setRecipeContent('{}')
    } finally {
      setLoading(false)
    }
  }

  const toggleExpand = (recipeIdx: number, stepIdx: number) => {
    const key = `${recipeIdx}-${stepIdx}`
    const newExpanded = new Set(expanded)
    if (newExpanded.has(key)) {
      newExpanded.delete(key)
    } else {
      newExpanded.add(key)
    }
    setExpanded(newExpanded)
  }

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text)
    alert('Copied to clipboard!')
  }

  if (loading) {
    return (
      <div className="min-h-screen">
        <Navbar />
        <div className="container mx-auto px-4 py-8 text-center text-gray-500">Loading...</div>
      </div>
    )
  }

  if (!recipe) {
    return (
      <div className="min-h-screen">
        <Navbar />
        <div className="container mx-auto px-4 py-8 text-center text-gray-600">Recipe not found</div>
      </div>
    )
  }

  let parsedContent: any = null
  try {
    parsedContent = JSON.parse(recipeContent)
  } catch (e) {
    // Invalid JSON
  }

  return (
    <div className="min-h-screen">
      <Navbar />
      <div className="container mx-auto px-4 py-8 max-w-6xl">
        <Link href="/recipes" className="text-gray-600 hover:text-gray-900 mb-4 inline-flex items-center gap-2 text-sm">
          <ArrowLeft className="w-4 h-4" />
          Back to Recipes
        </Link>

        <div className="mb-6">
          <div className="flex items-start justify-between mb-4">
            <div>
              <h1 className="text-xl font-semibold mb-2 text-gray-900">{recipe.name}</h1>
              <p className="text-gray-400 mb-2">{recipe.description}</p>
              <p className="text-sm text-gray-500 font-mono">{recipe.fileName}.json</p>
            </div>
            <div className="flex gap-2">
              <button
                onClick={() => setShowRawJson(!showRawJson)}
                className="px-4 py-2 border border-gray-700 text-gray-300 rounded-lg hover:bg-gray-800 hover:border-gray-600 transition-all flex items-center gap-2"
              >
                {showRawJson ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                {showRawJson ? 'Hide Raw JSON' : 'Show Raw JSON'}
              </button>
              <Link
                href={`/projects?selectRecipe=${recipeName}`}
                className="px-4 py-2 bg-blue-600 text-white rounded text-sm font-medium hover:bg-blue-700 flex items-center gap-2"
              >
                <CheckCircle2 className="w-4 h-4" />
                Use This Recipe
              </Link>
            </div>
          </div>
        </div>

      {showRawJson ? (
        <div className="border border-gray-300 rounded overflow-hidden bg-white">
          <div className="bg-gray-800 px-4 py-2 border-b border-gray-700 flex items-center justify-between">
            <span className="text-sm font-semibold text-gray-200">Raw JSON</span>
            <button
              onClick={() => copyToClipboard(recipeContent)}
              className="text-xs text-gray-400 hover:text-gray-200 flex items-center gap-1"
            >
              <Copy className="w-3 h-3" />
              Copy
            </button>
          </div>
          <pre className="p-4 bg-gray-100 text-gray-800 text-xs overflow-x-auto max-h-[800px] overflow-y-auto font-mono whitespace-pre-wrap break-words">
            {(() => {
              try {
                if (recipeContent && recipeContent.trim()) {
                  const parsed = typeof recipeContent === 'string' 
                    ? JSON.parse(recipeContent) 
                    : recipeContent
                  return JSON.stringify(parsed, null, 2)
                }
                return '{}'
              } catch (e) {
                return recipeContent || '{}'
              }
            })()}
          </pre>
        </div>
      ) : parsedContent && parsedContent.recipes && parsedContent.recipes.length > 0 ? (
        <div className="space-y-6">
          {parsedContent.recipes.map((r: any, recipeIdx: number) => (
            <div key={recipeIdx} className="border border-gray-300 rounded p-4 bg-white">
              <div className="mb-4">
                <h2 className="text-lg font-semibold mb-2 text-gray-900">{r.name || 'Unnamed Recipe'}</h2>
                {r.description && (
                  <p className="text-gray-400 mb-4">{r.description}</p>
                )}
                {r.rollbackOnError && (
                  <div className="text-sm bg-yellow-900/50 text-yellow-400 border border-yellow-800 px-3 py-1 rounded inline-block mb-4">
                    Rollback on error: {r.rollbackOnError}
                  </div>
                )}
              </div>

              {r.steps && r.steps.length > 0 && (
                <div>
                  <h3 className="text-lg font-semibold mb-3">Steps ({r.steps.length})</h3>
                  <div className="space-y-4">
                    {r.steps.map((step: any, stepIdx: number) => {
                      const expandKey = `${recipeIdx}-${stepIdx}`
                      const isExpanded = expanded.has(expandKey)
                      return (
                        <div key={stepIdx} className="border border-gray-200 rounded overflow-hidden bg-gray-50">
                          <button
                            onClick={() => toggleExpand(recipeIdx, stepIdx)}
                            className="w-full px-4 py-3 bg-gray-800 hover:bg-gray-700 flex items-center justify-between text-left transition-colors"
                          >
                            <span className="font-semibold text-gray-900">Step {stepIdx + 1}</span>
                            <span className="text-xs text-gray-400">
                              {isExpanded ? '▼' : '▶'} {step.match?.nodeType || 'Match'}
                            </span>
                          </button>

                          {isExpanded && (
                            <div className="p-4 space-y-4 border-t border-gray-200">
                              {/* Match Details */}
                              <div>
                                <h4 className="font-semibold mb-2 text-sm text-gray-300">Match Criteria:</h4>
                                <div className="bg-blue-950/50 border border-blue-800 p-3 rounded">
                                  <pre className="text-xs overflow-x-auto text-blue-300">
                                    {JSON.stringify(step.match, null, 2)}
                                  </pre>
                                </div>
                              </div>

                              {/* Actions */}
                              {step.actions && step.actions.length > 0 && (
                                <div>
                                  <h4 className="font-semibold mb-2 text-sm text-gray-300">
                                    Actions ({step.actions.length}):
                                  </h4>
                                  <div className="space-y-2">
                                    {step.actions.map((action: any, actionIdx: number) => (
                                      <div key={actionIdx} className="bg-green-950/50 border border-green-800 p-3 rounded">
                                        <pre className="text-xs overflow-x-auto text-green-300">
                                          {JSON.stringify(action, null, 2)}
                                        </pre>
                                      </div>
                                    ))}
                                  </div>
                                </div>
                              )}
                            </div>
                          )}
                        </div>
                      )
                    })}
                  </div>
                </div>
              )}

              {r.imports && (
                <div className="mt-4 pt-4 border-t border-gray-200">
                  <h4 className="font-semibold mb-2 text-sm text-gray-300">Import Modifications:</h4>
                  <div className="bg-gray-100 border border-gray-200 p-3 rounded">
                    <pre className="text-xs overflow-x-auto text-gray-300">
                      {JSON.stringify(r.imports, null, 2)}
                    </pre>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      ) : (
        <div className="border border-gray-300 rounded p-6 text-center text-gray-500 bg-white">
          Could not parse recipe content
        </div>
      )}
      </div>
    </div>
  )
}

