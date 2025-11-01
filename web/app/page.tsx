import Link from 'next/link'
import { ArrowRight, Code, Zap, Database } from 'lucide-react'
import Navbar from '@/components/Navbar'

export default function Home() {
  return (
    <div className="min-h-screen">
      <Navbar />

      <main className="container mx-auto px-4 py-16">
        <div className="text-center mb-16">
          <h2 className="text-5xl font-bold mb-4 text-white">
            Transform Java Codebases
            <br />
            <span className="bg-gradient-to-r from-blue-400 to-purple-500 bg-clip-text text-transparent">with AI-Powered Recipes</span>
          </h2>
          <p className="text-xl text-gray-400 max-w-2xl mx-auto mt-6">
            Enterprise-grade code transformation platform. Migrate frameworks, modernize codebases,
            and refactor at scale with intelligent recipe-based transformations.
          </p>
          <div className="mt-8 flex justify-center gap-4">
            <Link
              href="/projects/new"
              className="bg-gradient-to-r from-blue-500 to-purple-600 text-white px-6 py-3 rounded-lg font-semibold hover:from-blue-600 hover:to-purple-700 transition-all flex items-center gap-2 shadow-lg shadow-blue-500/25"
            >
              Get Started
              <ArrowRight className="w-4 h-4" />
            </Link>
            <Link
              href="/recipes"
              className="border border-gray-700 text-gray-300 px-6 py-3 rounded-lg font-semibold hover:bg-gray-800 hover:border-gray-600 transition-all"
            >
              Browse Recipes
            </Link>
          </div>
        </div>

        <div className="grid md:grid-cols-3 gap-8 mt-16">
          <div className="p-6 border border-gray-800 rounded-lg bg-gray-900/50 hover:bg-gray-900 hover:border-gray-700 transition-all">
            <Code className="w-12 h-12 text-blue-400 mb-4" />
            <h3 className="text-xl font-semibold mb-2 text-white">Recipe-Based</h3>
            <p className="text-gray-400">
              Define transformations using declarative JSON recipes. Match AST nodes and apply
              actions with precision.
            </p>
          </div>

          <div className="p-6 border border-gray-800 rounded-lg bg-gray-900/50 hover:bg-gray-900 hover:border-gray-700 transition-all">
            <Zap className="w-12 h-12 text-purple-400 mb-4" />
            <h3 className="text-xl font-semibold mb-2 text-white">AI-Powered</h3>
            <p className="text-gray-400">
              Generate recipes from natural language using RAG. Describe your transformation
              intent and get working recipes.
            </p>
          </div>

          <div className="p-6 border border-gray-800 rounded-lg bg-gray-900/50 hover:bg-gray-900 hover:border-gray-700 transition-all">
            <Database className="w-12 h-12 text-blue-400 mb-4" />
            <h3 className="text-xl font-semibold mb-2 text-white">Enterprise-Ready</h3>
            <p className="text-gray-400">
              Built for scale with job queuing, progress tracking, validation, and rollback
              capabilities.
            </p>
          </div>
        </div>
      </main>
    </div>
  )
}

