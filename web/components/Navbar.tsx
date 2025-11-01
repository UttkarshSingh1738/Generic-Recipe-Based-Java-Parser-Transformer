'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'

export default function Navbar() {
  const pathname = usePathname()

  return (
    <nav className="border-b border-gray-800 bg-gray-900/50 backdrop-blur-sm sticky top-0 z-50">
      <div className="container mx-auto px-4 py-4 flex justify-between items-center">
        <Link href="/">
          <h1 className="text-2xl font-bold bg-gradient-to-r from-blue-400 to-purple-500 bg-clip-text text-transparent">
            CodeForge
          </h1>
        </Link>
        <div className="space-x-6">
          <Link
            href="/projects"
            className={`transition-colors ${
              pathname?.startsWith('/projects')
                ? 'text-white font-semibold'
                : 'text-gray-400 hover:text-white'
            }`}
          >
            Projects
          </Link>
          <Link
            href="/recipes"
            className={`transition-colors ${
              pathname?.startsWith('/recipes')
                ? 'text-white font-semibold'
                : 'text-gray-400 hover:text-white'
            }`}
          >
            Recipes
          </Link>
          <Link
            href="/jobs"
            className={`transition-colors ${
              pathname?.startsWith('/jobs')
                ? 'text-white font-semibold'
                : 'text-gray-400 hover:text-white'
            }`}
          >
            Jobs
          </Link>
        </div>
      </div>
    </nav>
  )
}

