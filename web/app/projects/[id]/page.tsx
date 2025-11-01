'use client'

import { useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import Link from 'next/link'
import { Play, History, FileCode } from 'lucide-react'
import { projectApi, jobApi } from '@/lib/api'

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
    return <div className="container mx-auto px-4 py-8">Loading...</div>
  }

  if (!project) {
    return <div className="container mx-auto px-4 py-8">Project not found</div>
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <Link href="/projects" className="text-gray-600 hover:text-gray-900 mb-4 inline-block">
          ← Back to Projects
        </Link>
        <h1 className="text-3xl font-bold mb-2">{project.name}</h1>
        <p className="text-gray-600">{project.description || 'No description'}</p>
      </div>

      <div className="grid md:grid-cols-3 gap-6 mb-8">
        <div className="border rounded-lg p-6">
          <FileCode className="w-8 h-8 text-primary mb-2" />
          <div className="text-2xl font-bold">{project.fileCount || 0}</div>
          <div className="text-gray-600 text-sm">Files</div>
        </div>
        <div className="border rounded-lg p-6">
          <History className="w-8 h-8 text-primary mb-2" />
          <div className="text-2xl font-bold">{jobs.length}</div>
          <div className="text-gray-600 text-sm">Transformation Jobs</div>
        </div>
        <Link
          href={`/projects/${projectId}/transform`}
          className="border rounded-lg p-6 hover:border-primary transition-colors flex flex-col items-center justify-center"
        >
          <Play className="w-8 h-8 text-primary mb-2" />
          <div className="font-semibold">Run Transformation</div>
        </Link>
      </div>

      <div>
        <h2 className="text-2xl font-bold mb-4">Recent Jobs</h2>
        {jobs.length === 0 ? (
          <div className="border rounded-lg p-8 text-center text-gray-600">
            No transformation jobs yet. Create one to get started.
          </div>
        ) : (
          <div className="border rounded-lg overflow-hidden">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-sm font-semibold">Status</th>
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
                    </td>
                    <td className="px-6 py-4 text-sm">{job.recipeNames}</td>
                    <td className="px-6 py-4 text-sm">{job.filesTransformed || 0}</td>
                    <td className="px-6 py-4 text-sm">
                      {new Date(job.createdAt).toLocaleString()}
                    </td>
                    <td className="px-6 py-4">
                      <Link
                        href={`/jobs/${job.id}`}
                        className="text-primary hover:underline text-sm"
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

