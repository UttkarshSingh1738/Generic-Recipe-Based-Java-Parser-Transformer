'use client'

import { useState } from 'react'
import { FileCode, CheckCircle2, AlertCircle, Info, ArrowRight, Code } from 'lucide-react'

interface LogViewerProps {
  log: string
  recipeName: string
}

interface LogSection {
  type: 'info' | 'match' | 'action' | 'skip' | 'validation' | 'error' | 'copy' | 'write' | 'import' | 'summary'
  message: string
  details?: string
}

export default function LogViewer({ log, recipeName }: LogViewerProps) {
  const [showRaw, setShowRaw] = useState(false)
  
  // Parse log into structured sections
  const parseLog = (logText: string): LogSection[] => {
    const lines = logText.split('\n')
    const sections: LogSection[] = []
    
    for (const line of lines) {
      if (!line.trim()) continue
      
      if (line.includes('[MATCH]')) {
        sections.push({
          type: 'match',
          message: line.replace(/\[MATCH\]\s*/, ''),
        })
      } else if (line.includes('[ACTION]')) {
        sections.push({
          type: 'action',
          message: line.replace(/\[ACTION\]\s*/, ''),
        })
      } else if (line.includes('[SKIP]')) {
        sections.push({
          type: 'skip',
          message: line.replace(/\[SKIP\]\s*/, ''),
        })
      } else if (line.includes('[WRITE]')) {
        sections.push({
          type: 'write',
          message: line.replace(/\[WRITE\]\s*/, ''),
        })
      } else if (line.includes('[COPY]')) {
        sections.push({
          type: 'copy',
          message: line.replace(/\[COPY\]\s*/, ''),
        })
      } else if (line.includes('[IMPORT]')) {
        sections.push({
          type: 'import',
          message: line.replace(/\[IMPORT\]\s*/, ''),
        })
      } else if (line.includes('[VALIDATION]')) {
        sections.push({
          type: 'validation',
          message: line.replace(/\[VALIDATION\]\s*/, ''),
        })
      } else if (line.includes('[ERROR]') || line.includes('ERROR')) {
        sections.push({
          type: 'error',
          message: line.replace(/\[ERROR\]\s*/, ''),
        })
      } else if (line.includes('=== Transformation Summary ===')) {
        sections.push({
          type: 'summary',
          message: 'Transformation Summary',
        })
      } else if (line.includes('[INFO]') || line.includes('[PROCESS]') || line.includes('[PARSE-CHECK]')) {
        sections.push({
          type: 'info',
          message: line.replace(/\[(INFO|PROCESS|PARSE-CHECK|INITIAL-PARSE-CHECK|FINAL-PARSE-CHECK)\]\s*/, ''),
        })
      }
    }
    
    return sections
  }
  
  const sections = parseLog(log)
  
  // Count statistics
  const stats = {
    matches: sections.filter(s => s.type === 'match').length,
    actions: sections.filter(s => s.type === 'action').length,
    writes: sections.filter(s => s.type === 'write').length,
    skips: sections.filter(s => s.type === 'skip').length,
    errors: sections.filter(s => s.type === 'error').length,
  }
  
  const getIcon = (type: string) => {
    switch (type) {
      case 'match':
        return <CheckCircle2 className="w-4 h-4 text-blue-500" />
      case 'action':
        return <ArrowRight className="w-4 h-4 text-green-500" />
      case 'write':
        return <FileCode className="w-4 h-4 text-purple-500" />
      case 'skip':
        return <Info className="w-4 h-4 text-yellow-500" />
      case 'error':
        return <AlertCircle className="w-4 h-4 text-red-500" />
      case 'validation':
        return <CheckCircle2 className="w-4 h-4 text-indigo-500" />
      case 'import':
        return <Code className="w-4 h-4 text-cyan-500" />
      default:
        return <Info className="w-4 h-4 text-gray-500" />
    }
  }
  
  const getColor = (type: string) => {
    switch (type) {
      case 'match':
        return 'bg-blue-50 border-l-4 border-blue-500'
      case 'action':
        return 'bg-green-50 border-l-4 border-green-500'
      case 'write':
        return 'bg-purple-50 border-l-4 border-purple-500'
      case 'skip':
        return 'bg-yellow-50 border-l-4 border-yellow-500'
      case 'error':
        return 'bg-red-50 border-l-4 border-red-500'
      case 'validation':
        return 'bg-indigo-50 border-l-4 border-indigo-500'
      case 'import':
        return 'bg-cyan-50 border-l-4 border-cyan-500'
      case 'summary':
        return 'bg-gray-100 border-l-4 border-gray-500'
      default:
        return 'bg-gray-50'
    }
  }
  
  return (
    <div className="border rounded-lg overflow-hidden">
      <div className="bg-gray-50 px-4 py-3 border-b">
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-sm font-semibold text-gray-900">Transformation Log: {recipeName}</h3>
          <div className="flex gap-2">
            <button
              onClick={() => setShowRaw(!showRaw)}
              className="text-xs px-3 py-1 rounded bg-gray-200 text-gray-700 hover:bg-gray-300 transition-colors"
            >
              {showRaw ? 'Show Parsed' : 'Show Raw'}
            </button>
            <button
              onClick={() => {
                const blob = new Blob([log], { type: 'text/plain' })
                const url = URL.createObjectURL(blob)
                const a = document.createElement('a')
                a.href = url
                a.download = `${recipeName}.log`
                a.click()
                URL.revokeObjectURL(url)
              }}
              className="text-xs px-3 py-1 rounded bg-primary text-white hover:bg-primary/90 transition-colors"
            >
              Download
            </button>
          </div>
        </div>
        
        {/* Statistics Bar */}
        <div className="flex gap-4 text-xs text-gray-700">
          <div className="flex items-center gap-1">
            <span className="font-semibold">{stats.matches}</span> Matches
          </div>
          <div className="flex items-center gap-1">
            <span className="font-semibold text-green-600">{stats.actions}</span> Actions
          </div>
          <div className="flex items-center gap-1">
            <span className="font-semibold text-purple-600">{stats.writes}</span> Writes
          </div>
          {stats.skips > 0 && (
            <div className="flex items-center gap-1">
              <span className="font-semibold text-yellow-600">{stats.skips}</span> Skips
            </div>
          )}
          {stats.errors > 0 && (
            <div className="flex items-center gap-1">
              <span className="font-semibold text-red-600">{stats.errors}</span> Errors
            </div>
          )}
        </div>
      </div>
      
      {showRaw ? (
        <pre className="p-4 bg-gray-900 text-green-400 text-xs overflow-x-auto max-h-[600px] overflow-y-auto font-mono whitespace-pre-wrap break-words">
          {log}
        </pre>
      ) : (
        <div className="max-h-[600px] overflow-y-auto">
          <div className="divide-y divide-gray-200">
            {sections.map((section, idx) => (
              <div
                key={idx}
                className={`px-4 py-2 ${getColor(section.type)} hover:bg-opacity-70 transition-colors`}
              >
                <div className="flex items-start gap-3">
                  <div className="flex-shrink-0 mt-0.5">
                    {getIcon(section.type)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="text-sm text-gray-900 break-words font-mono">
                      {section.message}
                    </div>
                    {section.details && (
                      <div className="text-xs text-gray-600 mt-1 font-mono">
                        {section.details}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            ))}
            
            {sections.length === 0 && (
              <div className="px-4 py-8 text-center text-gray-500">
                No transformation events logged
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}

