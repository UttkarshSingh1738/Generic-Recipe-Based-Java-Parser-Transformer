'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { Calendar, CheckCircle, XCircle, Clock } from 'lucide-react'
import { jobApi } from '@/lib/api'
import Navbar from '@/components/Navbar'

interface Job {
  id: number
  projectId: number
  projectName: string
  recipeNames: string
  status: string
  filesTransformed: number
  filesFailed: number
  createdAt: string
  completedAt?: string
}

export default function JobsPage() {
  const [jobs, setJobs] = useState<Job[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadJobs()
    // Refresh every 5 seconds for running jobs
    const interval = setInterval(() => {
      loadJobs()
    }, 5000)
    return () => clearInterval(interval)
  }, [])

  const loadJobs = async () => {
    try {
      const response = await jobApi.getAll()
      setJobs(response.data)
    } catch (error) {
      console.error('Failed to load jobs', error)
    } finally {
      setLoading(false)
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

  return (
    <div className="min-h-screen">
      <Navbar />
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold mb-8 text-white">Transformation Jobs</h1>

        {loading ? (
          <div className="text-center py-12 text-gray-400">Loading jobs...</div>
        ) : jobs.length === 0 ? (
          <div className="text-center py-12 border border-gray-800 rounded-lg bg-gray-900/50">
            <p className="text-gray-400">No transformation jobs yet</p>
          </div>
        ) : (
          <div className="border border-gray-800 rounded-lg overflow-hidden bg-gray-900/50">
            <table className="w-full">
              <thead className="bg-gray-800 border-b border-gray-700">
                <tr>
                  <th className="px-6 py-3 text-left text-sm font-semibold text-gray-200">Status</th>
                  <th className="px-6 py-3 text-left text-sm font-semibold text-gray-200">Project</th>
                  <th className="px-6 py-3 text-left text-sm font-semibold text-gray-200">Recipes</th>
                  <th className="px-6 py-3 text-left text-sm font-semibold text-gray-200">Files</th>
                  <th className="px-6 py-3 text-left text-sm font-semibold text-gray-200">Created</th>
                  <th className="px-6 py-3 text-left text-sm font-semibold text-gray-200">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-800">
                {jobs.map((job) => (
                  <tr key={job.id} className="hover:bg-gray-800 transition-colors">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      {getStatusIcon(job.status)}
                      <span
                        className={`px-2 py-1 rounded text-xs font-medium ${
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
                  </td>
                  <td className="px-6 py-4">
                    <Link
                      href={`/projects/${job.projectId}`}
                      className="text-blue-400 hover:text-blue-300 hover:underline"
                    >
                      {job.projectName}
                    </Link>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-400">{job.recipeNames}</td>
                  <td className="px-6 py-4 text-sm text-gray-400">
                    {job.filesTransformed || 0}
                    {job.filesFailed > 0 && (
                      <span className="text-red-400 ml-2">({job.filesFailed} failed)</span>
                    )}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-400">
                    <div className="flex items-center gap-1">
                      <Calendar className="w-4 h-4" />
                      {new Date(job.createdAt).toLocaleString()}
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <Link
                      href={`/jobs/${job.id}`}
                      className="text-blue-400 hover:text-blue-300 hover:underline text-sm"
                    >
                      View Details
                    </Link>
                  </td>
                </tr>
              ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

