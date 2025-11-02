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
        <div className="container mx-auto px-4 py-8 text-center text-gray-400">Loading...</div>
      </div>
    )
  }

  if (!project) {
    return (
      <div className="min-h-screen">
        <Navbar />
        <div className="container mx-auto px-4 py-8 text-center text-gray-400">Project not found</div>
      </div>
    )
  }

  return (
    <div className="min-h-screen">
      <Navbar />
      <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <Link href="/projects" className="text-gray-400 hover:text-white mb-4 inline-block transition-colors">
          ← Back to Projects
        </Link>
        <h1 className="text-3xl font-bold mb-2 text-white">{project.name}</h1>
        <p className="text-gray-400">{project.description || 'No description'}</p>
      </div>

      <div className="grid md:grid-cols-3 gap-6 mb-8">
        <div className="border border-gray-800 rounded-lg p-6 bg-gray-900/50">
          <FileCode className="w-8 h-8 text-blue-400 mb-2" />
          <div className="text-2xl font-bold text-white">{project.fileCount || 0}</div>
          <div className="text-gray-400 text-sm">Files</div>
        </div>
        <div className="border border-gray-800 rounded-lg p-6 bg-gray-900/50">
          <History className="w-8 h-8 text-purple-400 mb-2" />
          <div className="text-2xl font-bold text-white">{jobs.length}</div>
          <div className="text-gray-400 text-sm">Transformation Jobs</div>
        </div>
        <Link
          href={`/projects/${projectId}/transform`}
          className="border border-gray-800 rounded-lg p-6 bg-gray-900/50 hover:bg-gray-900 hover:border-blue-500 transition-all flex flex-col items-center justify-center"
        >
          <Play className="w-8 h-8 text-blue-400 mb-2" />
          <div className="font-semibold text-white">Run Transformation</div>
        </Link>
      </div>

      <div>
        <h2 className="text-2xl font-bold mb-4 text-white">Recent Jobs</h2>
        {jobs.length === 0 ? (
          <div className="border border-gray-800 rounded-lg p-8 text-center text-gray-400 bg-gray-900/50">
            No transformation jobs yet. Create one to get started.
          </div>
        ) : (
          <div className="border border-gray-800 rounded-lg overflow-hidden bg-gray-900/50">
            <table className="w-full">
              <thead className="bg-gray-800 border-b border-gray-700">
                <tr>
                  <th className="px-6 py-3 text-left text-sm font-semibold text-gray-200">Status</th>
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
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-400">{job.recipeNames}</td>
                    <td className="px-6 py-4 text-sm text-gray-400">{job.filesTransformed || 0}</td>
                    <td className="px-6 py-4 text-sm text-gray-400">
                      {new Date(job.createdAt).toLocaleString()}
                    </td>
                    <td className="px-6 py-4">
                      <Link
                        href={`/jobs/${job.id}`}
                        className="text-blue-400 hover:text-blue-300 hover:underline text-sm"
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

