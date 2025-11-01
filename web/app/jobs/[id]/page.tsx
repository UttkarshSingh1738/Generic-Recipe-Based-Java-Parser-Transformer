'use client'

import { useEffect, useState } from 'react'
import { useParams } from 'next/navigation'
import Link from 'next/link'
import { CheckCircle, XCircle, Clock, FileCode, Plus, Minus, FileText } from 'lucide-react'
import { jobApi } from '@/lib/api'
import DiffViewer from '@/components/DiffViewer'

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
}

interface DiffData {
  originalPath: string
  transformedPath: string
  fileDiffs: Array<{
    relativePath: string
    lines: Array<{
      type: 'CONTEXT' | 'DELETED' | 'INSERTED'
      originalLineNumber: number
      transformedLineNumber: number
      content: string
    }>
    additions: number
    deletions: number
  }>
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
        return <CheckCircle className="w-5 h-5 text-green-600" />
      case 'FAILED':
        return <XCircle className="w-5 h-5 text-red-600" />
      case 'RUNNING':
        return <Clock className="w-5 h-5 text-blue-600 animate-spin" />
      default:
        return <Clock className="w-5 h-5 text-gray-600" />
    }
  }

  if (loading) {
    return <div className="container mx-auto px-4 py-8">Loading job details...</div>
  }

  if (!job) {
    return <div className="container mx-auto px-4 py-8">Job not found</div>
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <Link href="/jobs" className="text-gray-600 hover:text-gray-900 mb-4 inline-block">
          ← Back to Jobs
        </Link>
        <div className="flex items-center gap-4 mb-2">
          <h1 className="text-3xl font-bold">Transformation Job #{job.id}</h1>
          <div className="flex items-center gap-2">
            {getStatusIcon(job.status)}
            <span
              className={`px-3 py-1 rounded text-sm font-medium ${
                job.status === 'COMPLETED'
                  ? 'bg-green-100 text-green-800'
                  : job.status === 'FAILED'
                  ? 'bg-red-100 text-red-800'
                  : job.status === 'RUNNING'
                  ? 'bg-blue-100 text-blue-800'
                  : 'bg-gray-100 text-gray-800'
              }`}
            >
              {job.status}
            </span>
          </div>
        </div>
        <div className="text-gray-600">
          <Link href={`/projects/${job.projectId}`} className="hover:underline">
            Project: {job.projectName}
          </Link>
          {' • '}
          Created: {new Date(job.createdAt).toLocaleString()}
          {job.completedAt && ` • Completed: ${new Date(job.completedAt).toLocaleString()}`}
        </div>
      </div>

      <div className="grid md:grid-cols-3 gap-6 mb-8">
        <div className="border rounded-lg p-6">
          <div className="text-sm text-gray-600 mb-1">Recipes Applied</div>
          <div className="text-2xl font-bold">{recipeList.length}</div>
        </div>
        <div className="border rounded-lg p-6">
          <div className="text-sm text-gray-600 mb-1">Files Transformed</div>
          <div className="text-2xl font-bold">{job.filesTransformed || 0}</div>
        </div>
        <div className="border rounded-lg p-6">
          <div className="text-sm text-gray-600 mb-1">Files Failed</div>
          <div className={`text-2xl font-bold ${job.filesFailed > 0 ? 'text-red-600' : ''}`}>
            {job.filesFailed || 0}
          </div>
        </div>
      </div>

      {job.status === 'COMPLETED' && recipeList.length > 0 && (
        <div className="mb-8">
          <h2 className="text-xl font-semibold mb-4">Recipe Outputs</h2>
          <div className="flex gap-2 flex-wrap mb-6">
            {recipeList.map((recipe) => (
              <button
                key={recipe}
                onClick={() => setSelectedRecipe(recipe)}
                className={`px-4 py-2 rounded-lg border transition-colors ${
                  selectedRecipe === recipe
                    ? 'bg-primary text-white border-primary'
                    : 'bg-white hover:bg-gray-50'
                }`}
              >
                {recipe}
              </button>
            ))}
          </div>

          {selectedRecipe && (
            <div>
              <div className="mb-4 border-b">
                <div className="flex gap-4">
                  <button
                    onClick={() => setActiveTab('diff')}
                    className={`px-4 py-2 border-b-2 transition-colors flex items-center gap-2 ${
                      activeTab === 'diff'
                        ? 'border-primary text-primary font-semibold'
                        : 'border-transparent text-gray-600 hover:text-gray-900'
                    }`}
                  >
                    <FileCode className="w-4 h-4" />
                    Diff
                  </button>
                  <button
                    onClick={() => setActiveTab('log')}
                    className={`px-4 py-2 border-b-2 transition-colors flex items-center gap-2 ${
                      activeTab === 'log'
                        ? 'border-primary text-primary font-semibold'
                        : 'border-transparent text-gray-600 hover:text-gray-900'
                    }`}
                  >
                    <FileText className="w-4 h-4" />
                    Output Log
                  </button>
                </div>
              </div>

              {activeTab === 'diff' ? (
                loadingDiff ? (
                  <div className="border rounded-lg p-8 text-center">Loading diff...</div>
                ) : diff ? (
                  <div>
                    <div className="mb-4 flex items-center justify-between">
                      <h3 className="text-lg font-semibold">Diff for {selectedRecipe}</h3>
                      <div className="text-sm text-gray-600">
                        <span className="text-green-600">+{diff.totalAdditions}</span>
                        {' '}
                        <span className="text-red-600">-{diff.totalDeletions}</span>
                        {' '}
                        in {diff.changedFiles} file{diff.changedFiles !== 1 ? 's' : ''}
                      </div>
                    </div>
                    <DiffViewer diff={diff} />
                  </div>
                ) : (
                  <div className="border rounded-lg p-8 text-center text-gray-600">
                    No diff available for this recipe
                  </div>
                )
              ) : loadingLog ? (
                <div className="border rounded-lg p-8 text-center">Loading log...</div>
              ) : log ? (
                <div className="border rounded-lg overflow-hidden">
                  <div className="bg-gray-50 px-4 py-2 border-b flex items-center justify-between">
                    <h3 className="text-sm font-semibold">output.log</h3>
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
                      className="text-xs text-gray-600 hover:text-gray-900"
                    >
                      Download
                    </button>
                  </div>
                  <pre className="p-4 bg-gray-900 text-green-400 text-xs overflow-x-auto max-h-[600px] overflow-y-auto font-mono whitespace-pre-wrap break-words">
                    {log}
                  </pre>
                </div>
              ) : (
                <div className="border rounded-lg p-8 text-center text-gray-600">
                  No log available for this recipe
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {job.status === 'RUNNING' && (
        <div className="border rounded-lg p-8 text-center">
          <Clock className="w-12 h-12 text-blue-600 animate-spin mx-auto mb-4" />
          <p className="text-gray-600">Transformation in progress...</p>
          <p className="text-sm text-gray-500 mt-2">This page will auto-refresh</p>
        </div>
      )}
    </div>
  )
}

