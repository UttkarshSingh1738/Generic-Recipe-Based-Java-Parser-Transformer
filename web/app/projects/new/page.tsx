'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { Upload } from 'lucide-react'
import { projectApi } from '@/lib/api'

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
    <div className="container mx-auto px-4 py-8 max-w-2xl">
      <h1 className="text-3xl font-bold mb-8">Create New Project</h1>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div>
          <label className="block text-sm font-medium mb-2">Project Name</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
            placeholder="My Java Project"
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-2">Description</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={4}
            className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
            placeholder="Describe your project..."
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-2">
            Upload Source Code (ZIP)
          </label>
          <div className="border-2 border-dashed rounded-lg p-8 text-center">
            <Upload className="w-12 h-12 mx-auto text-gray-400 mb-4" />
            <input
              type="file"
              accept=".zip"
              onChange={(e) => setFile(e.target.files?.[0] || null)}
              className="hidden"
              id="file-upload"
            />
            <label
              htmlFor="file-upload"
              className="cursor-pointer text-primary hover:underline"
            >
              Click to upload ZIP file
            </label>
            {file && (
              <p className="mt-2 text-sm text-gray-600">{file.name}</p>
            )}
          </div>
        </div>

        <div className="flex gap-4">
          <button
            type="submit"
            disabled={uploading || !name}
            className="flex-1 bg-primary text-white px-6 py-3 rounded-lg font-semibold hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {uploading ? 'Creating...' : 'Create Project'}
          </button>
          <button
            type="button"
            onClick={() => router.back()}
            className="px-6 py-3 border rounded-lg hover:bg-gray-50"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}

