'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { Plus, Folder, Calendar } from 'lucide-react'
import { projectApi } from '@/lib/api'

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
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold">Projects</h1>
        <Link
          href="/projects/new"
          className="bg-primary text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-primary/90"
        >
          <Plus className="w-4 h-4" />
          New Project
        </Link>
      </div>

      {loading ? (
        <div className="text-center py-12">Loading projects...</div>
      ) : projects.length === 0 ? (
        <div className="text-center py-12 border rounded-lg">
          <Folder className="w-16 h-16 mx-auto text-gray-400 mb-4" />
          <h3 className="text-xl font-semibold mb-2">No projects yet</h3>
          <p className="text-gray-600 mb-4">Create your first project to get started</p>
          <Link
            href="/projects/new"
            className="inline-block bg-primary text-white px-4 py-2 rounded-lg hover:bg-primary/90"
          >
            Create Project
          </Link>
        </div>
      ) : (
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {projects.map((project) => (
            <Link
              key={project.id}
              href={`/projects/${project.id}`}
              className="border rounded-lg p-6 hover:border-primary transition-colors"
            >
              <Folder className="w-8 h-8 text-primary mb-3" />
              <h3 className="text-xl font-semibold mb-2">{project.name}</h3>
              <p className="text-gray-600 text-sm mb-4 line-clamp-2">
                {project.description || 'No description'}
              </p>
              <div className="flex items-center gap-4 text-sm text-gray-500">
                <span>{project.fileCount || 0} files</span>
                <span className="flex items-center gap-1">
                  <Calendar className="w-4 h-4" />
                  {new Date(project.createdAt).toLocaleDateString()}
                </span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}

