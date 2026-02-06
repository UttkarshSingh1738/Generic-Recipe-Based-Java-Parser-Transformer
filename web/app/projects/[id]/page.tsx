'use client'

import { useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import Link from 'next/link'
import { Play, History, FileCode } from 'lucide-react'
import { projectApi, jobApi } from '@/lib/api'
import Navbar from '@/components/Navbar'

interface Project {
  id: number
  name: string
  description: string
  fileCount: number
}

interface Job {
  id: number
  status: string
  recipeNames: string
  filesTransformed: number
  createdAt: string
}

export default function ProjectDetailPage() {
  const params = useParams()
  const router = useRouter()
  const projectId = Number(params.id)
  const [project, setProject] = useState<Project | null>(null)
  const [jobs, setJobs] = useState<Job[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (projectId) {
      loadProject()
      loadJobs()
    }
  }, [projectId])

  const loadProject = async () => {
    try {
      const response = await projectApi.getById(projectId)
      setProject(response.data)
    } catch (error) {
      console.error('Failed to load project', error)
    } finally {
      setLoading(false)
    }
  }

  const loadJobs = async () => {
    try {
      const response = await jobApi.getByProject(projectId)
      setJobs(response.data)
    } catch (error) {
      console.error('Failed to load jobs', error)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen">
        <Navbar />
        <div className="container mx-auto px-4 py-8 text-center text-gray-500">Loading...</div>
      </div>
    )
  }

  if (!project) {
    return (
      <div className="min-h-screen">
        <Navbar />
        <div className="container mx-auto px-4 py-8 text-center text-gray-600">Project not found</div>
      </div>
    )
  }

  return (
    <div className="min-h-screen">
      <Navbar />
      <div className="container mx-auto px-4 py-8">
      <div className="mb-6">
        <Link href="/projects" className="text-gray-600 hover:text-gray-900 text-sm mb-2 inline-block">
          ← Projects
        </Link>
        <h1 className="text-xl font-semibold mb-1 text-gray-900">{project.name}</h1>
        <p className="text-gray-600 text-sm">{project.description || 'No description'}</p>
      </div>

      <div className="grid md:grid-cols-3 gap-4 mb-6">
        <div className="border border-gray-300 rounded p-4 bg-white">
          <FileCode className="w-6 h-6 text-gray-500 mb-1" />
          <div className="text-lg font-semibold text-gray-900">{project.fileCount || 0}</div>
          <div className="text-gray-500 text-xs">Files</div>
        </div>
        <div className="border border-gray-300 rounded p-4 bg-white">
          <History className="w-6 h-6 text-gray-500 mb-1" />
          <div className="text-lg font-semibold text-gray-900">{jobs.length}</div>
          <div className="text-gray-500 text-xs">Jobs</div>
        </div>
        <Link
          href={`/projects/${projectId}/transform`}
          className="border border-gray-300 rounded p-4 bg-white hover:bg-gray-50 flex flex-col items-center justify-center"
        >
          <Play className="w-6 h-6 text-gray-500 mb-1" />
          <span className="font-medium text-gray-900 text-sm">Run transformation</span>
        </Link>
      </div>

      <div>
        <h2 className="text-lg font-semibold mb-3 text-gray-900">Recent jobs</h2>
        {jobs.length === 0 ? (
          <div className="border border-gray-300 rounded p-6 text-center text-gray-500 bg-white text-sm">
            No jobs yet. Run a transformation to start.
          </div>
        ) : (
          <div className="border border-gray-300 rounded overflow-hidden bg-white">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-4 py-2 text-left font-medium text-gray-700">Status</th>
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
                    </td>
                    <td className="px-4 py-2 text-gray-600">{job.recipeNames}</td>
                    <td className="px-4 py-2 text-gray-600">{job.filesTransformed || 0}</td>
                    <td className="px-4 py-2 text-gray-600">
                      {new Date(job.createdAt).toLocaleString()}
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
    </div></div>
  )
}

