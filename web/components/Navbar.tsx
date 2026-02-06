'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'

export default function Navbar() {
  const pathname = usePathname()

  return (
    <nav className="border-b border-gray-300 bg-white sticky top-0 z-50">
      <div className="container mx-auto px-4 py-3 flex justify-between items-center">
        <Link href="/">
          <h1 className="text-xl font-semibold text-gray-800">
            Transformer API
          </h1>
        </Link>
        <div className="space-x-6">
          <Link
            href="/projects"
            className={`transition-colors ${
              pathname?.startsWith('/projects')
                ? 'text-gray-900 font-semibold'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Projects
          </Link>
          <Link
            href="/recipes"
            className={`transition-colors ${
              pathname?.startsWith('/recipes')
                ? 'text-gray-900 font-semibold'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Recipes
          </Link>
          <Link
            href="/jobs"
            className={`transition-colors ${
              pathname?.startsWith('/jobs')
                ? 'text-gray-900 font-semibold'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Jobs
          </Link>
        </div>
      </div>
    </nav>
  )
}

