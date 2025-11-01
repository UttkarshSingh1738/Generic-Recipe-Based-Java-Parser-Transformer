'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { Calendar, CheckCircle, XCircle, Clock } from 'lucide-react'
import { jobApi } from '@/lib/api'

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
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">Transformation Jobs</h1>

      {loading ? (
        <div className="text-center py-12">Loading jobs...</div>
      ) : jobs.length === 0 ? (
        <div className="text-center py-12 border rounded-lg">
          <p className="text-gray-600">No transformation jobs yet</p>
        </div>
      ) : (
        <div className="border rounded-lg overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-sm font-semibold">Status</th>
                <th className="px-6 py-3 text-left text-sm font-semibold">Project</th>
                <th className="px-6 py-3 text-left text-sm font-semibold">Recipes</th>
                <th className="px-6 py-3 text-left text-sm font-semibold">Files</th>
                <th className="px-6 py-3 text-left text-sm font-semibold">Created</th>
                <th className="px-6 py-3 text-left text-sm font-semibold">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {jobs.map((job) => (
                <tr key={job.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      {getStatusIcon(job.status)}
                      <span
                        className={`px-2 py-1 rounded text-sm ${
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
                  </td>
                  <td className="px-6 py-4">
                    <Link
                      href={`/projects/${job.projectId}`}
                      className="text-primary hover:underline"
                    >
                      {job.projectName}
                    </Link>
                  </td>
                  <td className="px-6 py-4 text-sm">{job.recipeNames}</td>
                  <td className="px-6 py-4 text-sm">
                    {job.filesTransformed || 0}
                    {job.filesFailed > 0 && (
                      <span className="text-red-600 ml-2">({job.filesFailed} failed)</span>
                    )}
                  </td>
                  <td className="px-6 py-4 text-sm">
                    <div className="flex items-center gap-1">
                      <Calendar className="w-4 h-4" />
                      {new Date(job.createdAt).toLocaleString()}
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <Link
                      href={`/jobs/${job.id}`}
                      className="text-primary hover:underline text-sm"
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
  )
}

