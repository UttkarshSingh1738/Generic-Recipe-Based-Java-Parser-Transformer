'use client'

import { useEffect, useState } from 'react'
import { useParams } from 'next/navigation'
import Link from 'next/link'
import { CheckCircle, XCircle, Clock, FileCode, Plus, Minus, FileText, Loader2 } from 'lucide-react'
import { jobApi } from '@/lib/api'
import DiffViewer from '@/components/DiffViewer'
import Navbar from '@/components/Navbar'

interface Job {
  id: number
  projectId: number
  projectName: string
  recipeNames: string
  status: string
  filesTransformed: number
  filesFailed: number
  outputPath: string
  createdAt: string
  completedAt?: string
  errorMessage?: string
}

interface DiffData {
  originalPath: string
  transformedPath: string
  fileDiffs: any[]
  totalAdditions: number
  totalDeletions: number
  changedFiles: number
}

export default function JobDetailPage() {
  const params = useParams()
  const jobId = Number(params.id)
  const [job, setJob] = useState<Job | null>(null)
  const [selectedRecipe, setSelectedRecipe] = useState<string | null>(null)
  const [diff, setDiff] = useState<DiffData | null>(null)
  const [log, setLog] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadingDiff, setLoadingDiff] = useState(false)
  const [loadingLog, setLoadingLog] = useState(false)
  const [recipeList, setRecipeList] = useState<string[]>([])
  const [activeTab, setActiveTab] = useState<'diff' | 'log'>('diff')

  useEffect(() => {
    if (jobId) {
      loadJob()
    }
  }, [jobId])

  // Auto-refresh while job is pending or running
  useEffect(() => {
    if (!job || (job.status !== 'PENDING' && job.status !== 'RUNNING')) {
      return
    }

    const interval = setInterval(() => {
      loadJob()
    }, 3000) // Refresh every 3 seconds

    return () => clearInterval(interval)
  }, [job?.status])

  useEffect(() => {
    if (job && job.status === 'COMPLETED' && selectedRecipe) {
      if (activeTab === 'diff') {
        loadDiff(selectedRecipe)
      } else if (activeTab === 'log') {
        loadLog(selectedRecipe)
      }
    }
  }, [job, selectedRecipe, activeTab])

  const loadJob = async () => {
    try {
      const response = await jobApi.getById(jobId)
      const jobData = response.data
      setJob(jobData)
      
      // Extract recipe names
      if (jobData.recipeNames) {
        const recipes = jobData.recipeNames.split(',').map((r: string) => r.trim()).filter(Boolean)
        setRecipeList(recipes)
        if (recipes.length > 0 && jobData.status === 'COMPLETED') {
          setSelectedRecipe(recipes[0])
        }
      }
    } catch (error) {
      console.error('Failed to load job', error)
    } finally {
      setLoading(false)
    }
  }

  const loadDiff = async (recipeName: string) => {
    setLoadingDiff(true)
    try {
      const response = await jobApi.getDiff(jobId, recipeName)
      setDiff(response.data)
    } catch (error) {
      console.error('Failed to load diff', error)
      setDiff(null)
    } finally {
      setLoadingDiff(false)
    }
  }

  const loadLog = async (recipeName: string) => {
    setLoadingLog(true)
    try {
      const response = await jobApi.getLog(jobId, recipeName)
      setLog(response.data)
    } catch (error) {
      console.error('Failed to load log', error)
      setLog(null)
    } finally {
      setLoadingLog(false)
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return <CheckCircle className="w-5 h-5 text-green-400" />
      case 'FAILED':
        return <XCircle className="w-5 h-5 text-red-400" />
      case 'RUNNING':
        return <Clock className="w-5 h-5 text-blue-400 animate-spin" />
      default:
        return <Clock className="w-5 h-5 text-gray-400" />
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen">
        <Navbar />
        <div className="container mx-auto px-4 py-8 text-center text-gray-400">Loading job details...</div>
      </div>
    )
  }

  if (!job) {
    return (
      <div className="min-h-screen">
        <Navbar />
        <div className="container mx-auto px-4 py-8 text-center text-gray-400">Job not found</div>
      </div>
    )
  }

  return (
    <div className="min-h-screen">
      <Navbar />
      <div className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <Link href="/jobs" className="text-gray-400 hover:text-white mb-4 inline-block transition-colors">
            ← Back to Jobs
          </Link>
          <div className="flex items-center gap-4 mb-2">
            <h1 className="text-3xl font-bold text-white">Transformation Job #{job.id}</h1>
            <div className="flex items-center gap-2">
              {getStatusIcon(job.status)}
              <span
                className={`px-3 py-1 rounded text-xs font-medium ${
                  job.status === 'COMPLETED'
                    ? 'bg-green-900/50 text-green-400 border border-green-800'
                    : job.status === 'FAILED'
                    ? 'bg-red-900/50 text-red-400 border border-red-800'
                    : job.status === 'RUNNING'
                    ? 'bg-blue-900/50 text-blue-400 border border-blue-800'
                    : 'bg-gray-800 text-gray-400 border border-gray-700'
                }`}
              >
                {job.status}
              </span>
            </div>
          </div>
          <div className="text-gray-400">
            <Link href={`/projects/${job.projectId}`} className="text-blue-400 hover:text-blue-300 hover:underline">
              Project: {job.projectName}
            </Link>
            {' • '}
            Created: {new Date(job.createdAt).toLocaleString()}
            {job.completedAt && ` • Completed: ${new Date(job.completedAt).toLocaleString()}`}
          </div>
          {job.errorMessage && (
            <div className="mt-4 p-3 bg-red-900/20 border border-red-800 text-red-400 rounded-lg text-sm">
              Error: {job.errorMessage}
            </div>
          )}
        </div>

        <div className="grid md:grid-cols-3 gap-6 mb-8">
          <div className="border border-gray-800 rounded-lg p-6 bg-gray-900/50">
            <div className="text-sm text-gray-400 mb-1">Recipes Applied</div>
            <div className="text-2xl font-bold text-white">{recipeList.length}</div>
          </div>
          <div className="border border-gray-800 rounded-lg p-6 bg-gray-900/50">
            <div className="text-sm text-gray-400 mb-1">Files Transformed</div>
            <div className="text-2xl font-bold text-green-400">{job.filesTransformed || 0}</div>
          </div>
          <div className="border border-gray-800 rounded-lg p-6 bg-gray-900/50">
            <div className="text-sm text-gray-400 mb-1">Files Failed</div>
            <div className={`text-2xl font-bold ${job.filesFailed > 0 ? 'text-red-400' : 'text-gray-400'}`}>
              {job.filesFailed || 0}
            </div>
          </div>
        </div>

        {job.status === 'PENDING' || job.status === 'RUNNING' ? (
          <div className="border border-gray-800 rounded-lg p-8 text-center bg-gray-900/50">
            <Loader2 className="w-12 h-12 animate-spin text-blue-400 mx-auto mb-4" />
            <p className="text-xl font-semibold mb-2 text-white">Transformation in progress...</p>
            <p className="text-gray-400">
              This page will update automatically when the job is completed.
            </p>
          </div>
        ) : (
          <div>
            <h2 className="text-2xl font-bold mb-4 text-white">Transformation Results</h2>

            <div className="mb-6">
              <div className="flex gap-2 border-b border-gray-800 pb-2 overflow-x-auto">
                {recipeList.map((recipe) => (
                  <button
                    key={recipe}
                    onClick={() => setSelectedRecipe(recipe)}
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors flex-shrink-0 ${
                      selectedRecipe === recipe
                        ? 'bg-gradient-to-r from-blue-500 to-purple-600 text-white shadow-lg shadow-blue-500/25'
                        : 'bg-gray-800 text-gray-300 hover:bg-gray-700 border border-gray-700'
                    }`}
                  >
                    {recipe}
                  </button>
                ))}
              </div>
            </div>

            {selectedRecipe && (
              <div>
                <div className="mb-4 border-b border-gray-800">
                  <div className="flex gap-4">
                    <button
                      onClick={() => setActiveTab('diff')}
                      className={`px-4 py-2 border-b-2 transition-colors flex items-center gap-2 ${
                        activeTab === 'diff'
                          ? 'border-blue-500 text-blue-400 font-semibold'
                          : 'border-transparent text-gray-400 hover:text-gray-300'
                      }`}
                    >
                      <FileCode className="w-4 h-4" />
                      Diff
                    </button>
                    <button
                      onClick={() => setActiveTab('log')}
                      className={`px-4 py-2 border-b-2 transition-colors flex items-center gap-2 ${
                        activeTab === 'log'
                          ? 'border-blue-500 text-blue-400 font-semibold'
                          : 'border-transparent text-gray-400 hover:text-gray-300'
                      }`}
                    >
                      <FileText className="w-4 h-4" />
                      Output Log
                    </button>
                  </div>
                </div>

                {activeTab === 'diff' ? (
                  loadingDiff ? (
                    <div className="border border-gray-800 rounded-lg p-8 text-center bg-gray-900/50 text-gray-400">Loading diff...</div>
                  ) : diff ? (
                    <div>
                      <div className="mb-4 flex items-center justify-between">
                        <h3 className="text-lg font-semibold text-white">Diff for {selectedRecipe}</h3>
                        <div className="text-sm text-gray-400">
                          <span className="text-green-400">+{diff.totalAdditions}</span>
                          {' '}
                          <span className="text-red-400">-{diff.totalDeletions}</span>
                          {' '}
                          in {diff.changedFiles} file{diff.changedFiles !== 1 ? 's' : ''}
                        </div>
                      </div>
                      <DiffViewer diff={diff} />
                    </div>
                  ) : (
                    <div className="border border-gray-800 rounded-lg p-8 text-center text-gray-400 bg-gray-900/50">
                      No diff available for this recipe
                    </div>
                  )
                ) : loadingLog ? (
                  <div className="border border-gray-800 rounded-lg p-8 text-center bg-gray-900/50 text-gray-400">Loading log...</div>
                ) : log ? (
                  <div className="border border-gray-800 rounded-lg overflow-hidden bg-gray-900">
                    <div className="bg-gray-800 px-4 py-2 border-b border-gray-700 flex items-center justify-between">
                      <h3 className="text-sm font-semibold text-gray-200">output.log</h3>
                      <button
                        onClick={() => {
                          const blob = new Blob([log], { type: 'text/plain' })
                          const url = URL.createObjectURL(blob)
                          const a = document.createElement('a')
                          a.href = url
                          a.download = `${selectedRecipe}.log`
                          a.click()
                          URL.revokeObjectURL(url)
                        }}
                        className="text-xs text-gray-400 hover:text-gray-200"
                      >
                        Download
                      </button>
                    </div>
                    <pre className="p-4 bg-gray-950 text-green-400 text-xs overflow-x-auto max-h-[600px] overflow-y-auto font-mono whitespace-pre-wrap break-words">
                      {log}
                    </pre>
                  </div>
                ) : (
                  <div className="border border-gray-800 rounded-lg p-8 text-center text-gray-400 bg-gray-900/50">
                    No log available for this recipe
                  </div>
                )}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}

