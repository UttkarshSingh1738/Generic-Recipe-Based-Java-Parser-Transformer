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
      <h1 className="text-xl font-semibold mb-6 text-gray-900">New project</h1>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium mb-1 text-gray-700">Name</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="My Java Project"
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1 text-gray-700">Description</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={3}
            className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="Optional..."
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1 text-gray-700">Source (ZIP)</label>
          <div className="border border-dashed border-gray-400 rounded p-6 text-center bg-gray-50">
            <Upload className="w-10 h-10 mx-auto text-gray-400 mb-2" />
            <input
              type="file"
              accept=".zip"
              onChange={(e) => setFile(e.target.files?.[0] || null)}
              className="hidden"
              id="file-upload"
            />
            <label
              htmlFor="file-upload"
              className="cursor-pointer text-blue-600 hover:underline text-sm"
            >
              Choose ZIP file
            </label>
            {file && (
              <p className="mt-2 text-sm text-gray-600">{file.name}</p>
            )}
          </div>
        </div>

        <div className="flex gap-3">
          <button
            type="submit"
            disabled={uploading || !name}
            className="bg-blue-600 text-white px-4 py-2 rounded text-sm font-medium hover:bg-blue-700 disabled:opacity-50"
          >
            {uploading ? 'Creating...' : 'Create'}
          </button>
          <button
            type="button"
            onClick={() => router.back()}
            className="border border-gray-300 text-gray-700 px-4 py-2 rounded text-sm hover:bg-gray-50"
          >
            Cancel
          </button>
        </div>
      </form>
      </div>
    </div>
  )
}

