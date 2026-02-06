'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { Plus, Folder, Calendar } from 'lucide-react'
import { projectApi } from '@/lib/api'
import Navbar from '@/components/Navbar'

interface Project {
  id: number
  name: string
  description: string
  fileCount: number
  createdAt: string
}

export default function ProjectsPage() {
  const [projects, setProjects] = useState<Project[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadProjects()
  }, [])

  const loadProjects = async () => {
    try {
      const response = await projectApi.getAll()
      setProjects(response.data)
    } catch (error) {
      console.error('Failed to load projects', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen">
      <Navbar />
      <div className="container mx-auto px-4 py-8">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-xl font-semibold text-gray-900">Projects</h1>
        <Link
          href="/projects/new"
          className="bg-blue-600 text-white px-4 py-2 rounded text-sm font-medium flex items-center gap-2 hover:bg-blue-700"
        >
          <Plus className="w-4 h-4" />
          New project
        </Link>
      </div>

      {loading ? (
        <div className="text-center py-12 text-gray-500">Loading...</div>
      ) : projects.length === 0 ? (
        <div className="text-center py-12 border border-gray-300 rounded bg-white">
          <Folder className="w-12 h-12 mx-auto text-gray-400 mb-3" />
          <h3 className="font-medium text-gray-900 mb-1">No projects</h3>
          <p className="text-gray-600 text-sm mb-4">Create a project and upload a ZIP to test the API.</p>
          <Link
            href="/projects/new"
            className="inline-block bg-blue-600 text-white px-4 py-2 rounded text-sm font-medium hover:bg-blue-700"
          >
            New project
          </Link>
        </div>
      ) : (
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
          {projects.map((project) => (
            <Link
              key={project.id}
              href={`/projects/${project.id}`}
              className="border border-gray-300 rounded p-4 bg-white hover:bg-gray-50"
            >
              <Folder className="w-6 h-6 text-gray-500 mb-2" />
              <h3 className="font-medium text-gray-900 mb-1">{project.name}</h3>
              <p className="text-gray-600 text-sm mb-3 line-clamp-2">
                {project.description || 'No description'}
              </p>
              <div className="flex items-center gap-4 text-xs text-gray-500">
                <span>{project.fileCount || 0} files</span>
                <span className="flex items-center gap-1">
                  <Calendar className="w-3 h-3" />
                  {new Date(project.createdAt).toLocaleDateString()}
                </span>
              </div>
            </Link>
          ))}
        </div>
      )}
      </div>
    </div>
  )
}

