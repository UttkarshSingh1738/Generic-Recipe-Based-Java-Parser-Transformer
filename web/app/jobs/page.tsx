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
        <h1 className="text-xl font-semibold mb-6 text-gray-900">Jobs</h1>

        {loading ? (
          <div className="text-center py-12 text-gray-500">Loading...</div>
        ) : jobs.length === 0 ? (
          <div className="text-center py-12 border border-gray-300 rounded bg-white">
            <p className="text-gray-500 text-sm">No jobs yet</p>
          </div>
        ) : (
          <div className="border border-gray-300 rounded overflow-hidden bg-white">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-4 py-2 text-left font-medium text-gray-700">Status</th>
                  <th className="px-4 py-2 text-left font-medium text-gray-700">Project</th>
                  <th className="px-4 py-2 text-left font-medium text-gray-700">Recipes</th>
                  <th className="px-4 py-2 text-left font-medium text-gray-700">Files</th>
                  <th className="px-4 py-2 text-left font-medium text-gray-700">Created</th>
                  <th className="px-4 py-2 text-left font-medium text-gray-700">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {jobs.map((job) => (
                  <tr key={job.id} className="hover:bg-gray-50">
                  <td className="px-4 py-2">
                    <div className="flex items-center gap-2">
                      {getStatusIcon(job.status)}
                      <span
                        className={`px-2 py-0.5 rounded text-xs ${
                          job.status === 'COMPLETED'
                            ? 'bg-green-100 text-green-800'
                            : job.status === 'FAILED'
                            ? 'bg-red-100 text-red-800'
                            : job.status === 'RUNNING'
                            ? 'bg-blue-100 text-blue-800'
                            : 'bg-gray-100 text-gray-700'
                        }`}
                      >
                        {job.status}
                      </span>
                    </div>
                  </td>
                  <td className="px-4 py-2">
                    <Link
                      href={`/projects/${job.projectId}`}
                      className="text-blue-600 hover:underline"
                    >
                      {job.projectName}
                    </Link>
                  </td>
                  <td className="px-4 py-2 text-gray-600">{job.recipeNames}</td>
                  <td className="px-4 py-2 text-gray-600">
                    {job.filesTransformed || 0}
                    {job.filesFailed > 0 && (
                      <span className="text-red-600 ml-1">({job.filesFailed} failed)</span>
                    )}
                  </td>
                  <td className="px-4 py-2 text-gray-600">
                    <div className="flex items-center gap-1">
                      <Calendar className="w-3 h-3" />
                      {new Date(job.createdAt).toLocaleString()}
                    </div>
                  </td>
                  <td className="px-4 py-2">
                    <Link
                      href={`/jobs/${job.id}`}
                      className="text-blue-600 hover:underline"
                    >
                      View
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

