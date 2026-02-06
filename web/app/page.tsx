import Link from 'next/link'
import { ArrowRight, Folder, BookOpen, List } from 'lucide-react'
import Navbar from '@/components/Navbar'

export default function Home() {
  return (
    <div className="min-h-screen">
      <Navbar />

      <main className="container mx-auto px-4 py-10">
        <div className="mb-10">
          <h2 className="text-2xl font-semibold mb-2 text-gray-900">
            Recipe-based Java transformation API
          </h2>
          <p className="text-gray-600 max-w-xl">
            Upload a project, pick recipes, run transformations, and inspect diffs and logs.
          </p>
          <div className="mt-6 flex gap-3">
            <Link
              href="/projects/new"
              className="bg-blue-600 text-white px-4 py-2 rounded text-sm font-medium hover:bg-blue-700 flex items-center gap-2"
            >
              New project
              <ArrowRight className="w-4 h-4" />
            </Link>
            <Link
              href="/recipes"
              className="border border-gray-300 text-gray-700 px-4 py-2 rounded text-sm font-medium hover:bg-gray-50"
            >
              Recipes
            </Link>
          </div>
        </div>

        <div className="grid md:grid-cols-3 gap-4">
          <Link href="/projects" className="p-4 border border-gray-300 rounded bg-white hover:bg-gray-50">
            <Folder className="w-8 h-8 text-gray-500 mb-2" />
            <h3 className="font-medium text-gray-900">Projects</h3>
            <p className="text-sm text-gray-600">Upload and manage test projects.</p>
          </Link>
          <Link href="/recipes" className="p-4 border border-gray-300 rounded bg-white hover:bg-gray-50">
            <BookOpen className="w-8 h-8 text-gray-500 mb-2" />
            <h3 className="font-medium text-gray-900">Recipes</h3>
            <p className="text-sm text-gray-600">Browse and run transformation recipes.</p>
          </Link>
          <Link href="/jobs" className="p-4 border border-gray-300 rounded bg-white hover:bg-gray-50">
            <List className="w-8 h-8 text-gray-500 mb-2" />
            <h3 className="font-medium text-gray-900">Jobs</h3>
            <p className="text-sm text-gray-600">View run history and output.</p>
          </Link>
        </div>
      </main>
    </div>
  )
}

