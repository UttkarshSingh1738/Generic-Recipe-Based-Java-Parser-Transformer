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
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-white">Projects</h1>
        <Link
          href="/projects/new"
          className="bg-gradient-to-r from-blue-500 to-purple-600 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:from-blue-600 hover:to-purple-700 transition-all shadow-lg shadow-blue-500/25"
        >
          <Plus className="w-4 h-4" />
          New Project
        </Link>
      </div>

      {loading ? (
        <div className="text-center py-12 text-gray-400">Loading projects...</div>
      ) : projects.length === 0 ? (
        <div className="text-center py-12 border border-gray-800 rounded-lg bg-gray-900/50">
          <Folder className="w-16 h-16 mx-auto text-gray-400 mb-4" />
          <h3 className="text-xl font-semibold mb-2 text-white">No projects yet</h3>
          <p className="text-gray-400 mb-4">Create your first project to get started</p>
          <Link
            href="/projects/new"
            className="inline-block bg-gradient-to-r from-blue-500 to-purple-600 text-white px-4 py-2 rounded-lg hover:from-blue-600 hover:to-purple-700 transition-all shadow-lg shadow-blue-500/25"
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
              className="border border-gray-800 rounded-lg p-6 bg-gray-900/50 hover:bg-gray-900 hover:border-gray-700 transition-all"
            >
              <Folder className="w-8 h-8 text-blue-400 mb-3" />
              <h3 className="text-xl font-semibold mb-2 text-white">{project.name}</h3>
              <p className="text-gray-400 text-sm mb-4 line-clamp-2">
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
    </div>
  )
}

