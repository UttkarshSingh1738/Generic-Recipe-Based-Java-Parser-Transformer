'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { Upload } from 'lucide-react'
import { projectApi } from '@/lib/api'
import Navbar from '@/components/Navbar'

export default function NewProjectPage() {
  const router = useRouter()
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [file, setFile] = useState<File | null>(null)
  const [uploading, setUploading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setUploading(true)

    try {
      // Create project
      const projectResponse = await projectApi.create({
        name,
        description,
        storagePath: '',
        sourcePath: '',
        fileCount: 0,
      })

      const projectId = projectResponse.data.id

      // Upload file if provided
      if (file) {
        try {
          await projectApi.upload(projectId, file)
        } catch (uploadError: any) {
          console.error('Upload failed', uploadError)
          alert('Project created but upload failed: ' + (uploadError.response?.data?.error || uploadError.message))
          router.push(`/projects/${projectId}`)
          return
        }
      }

      router.push(`/projects/${projectId}`)
    } catch (error: any) {
      console.error('Failed to create project', error)
      alert('Failed to create project: ' + (error.response?.data?.error || error.message))
    } finally {
      setUploading(false)
    }
  }

  return (
    <div className="min-h-screen">
      <Navbar />
      <div className="container mx-auto px-4 py-8 max-w-2xl">
      <h1 className="text-3xl font-bold mb-8 text-white">Create New Project</h1>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div>
          <label className="block text-sm font-medium mb-2 text-gray-300">Project Name</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            className="w-full px-4 py-2 bg-gray-900 border border-gray-700 text-white rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent placeholder-gray-500"
            placeholder="My Java Project"
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-2 text-gray-300">Description</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={4}
            className="w-full px-4 py-2 bg-gray-900 border border-gray-700 text-white rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent placeholder-gray-500"
            placeholder="Describe your project..."
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-2 text-gray-300">
            Upload Source Code (ZIP)
          </label>
          <div className="border-2 border-dashed border-gray-700 rounded-lg p-8 text-center bg-gray-900/50 hover:border-gray-600 transition-colors">
            <Upload className="w-12 h-12 mx-auto text-gray-500 mb-4" />
            <input
              type="file"
              accept=".zip"
              onChange={(e) => setFile(e.target.files?.[0] || null)}
              className="hidden"
              id="file-upload"
            />
            <label
              htmlFor="file-upload"
              className="cursor-pointer text-blue-400 hover:text-blue-300 hover:underline"
            >
              Click to upload ZIP file
            </label>
            {file && (
              <p className="mt-2 text-sm text-gray-400">{file.name}</p>
            )}
          </div>
        </div>

        <div className="flex gap-4">
          <button
            type="submit"
            disabled={uploading || !name}
            className="flex-1 bg-gradient-to-r from-blue-500 to-purple-600 text-white px-6 py-3 rounded-lg font-semibold hover:from-blue-600 hover:to-purple-700 transition-all shadow-lg shadow-blue-500/25 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {uploading ? 'Creating...' : 'Create Project'}
          </button>
          <button
            type="button"
            onClick={() => router.back()}
            className="px-6 py-3 border border-gray-700 text-gray-300 rounded-lg hover:bg-gray-800 hover:border-gray-600 transition-all"
          >
            Cancel
          </button>
        </div>
      </form>
      </div>
    </div>
  )
}

