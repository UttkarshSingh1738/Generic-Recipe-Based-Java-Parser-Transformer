'use client'

import { useState } from 'react'
import { Plus, Minus, FileCode } from 'lucide-react'

interface DiffLine {
  type: 'CONTEXT' | 'DELETED' | 'INSERTED'
  originalLineNumber: number
  transformedLineNumber: number
  content: string
}

interface FileDiff {
  relativePath: string
  lines: DiffLine[]
  additions: number
  deletions: number
}

interface DiffData {
  originalPath: string
  transformedPath: string
  fileDiffs: FileDiff[]
  totalAdditions: number
  totalDeletions: number
  changedFiles: number
}

interface DiffViewerProps {
  diff: DiffData
}

export default function DiffViewer({ diff }: DiffViewerProps) {
  const [expandedFiles, setExpandedFiles] = useState<Set<string>>(new Set())

  const toggleFile = (filePath: string) => {
    const newExpanded = new Set(expandedFiles)
    if (newExpanded.has(filePath)) {
      newExpanded.delete(filePath)
    } else {
      newExpanded.add(filePath)
    }
    setExpandedFiles(newExpanded)
  }

  const getLineColor = (type: string) => {
    switch (type) {
      case 'DELETED':
        return 'bg-red-50 border-l-4 border-red-500'
      case 'INSERTED':
        return 'bg-green-50 border-l-4 border-green-500'
      default:
        return 'bg-gray-50'
    }
  }

  const getLineIcon = (type: string) => {
    switch (type) {
      case 'DELETED':
        return <Minus className="w-4 h-4 text-red-600" />
      case 'INSERTED':
        return <Plus className="w-4 h-4 text-green-600" />
      default:
        return <span className="w-4 h-4" />
    }
  }

  if (!diff.fileDiffs || diff.fileDiffs.length === 0) {
    return (
      <div className="border rounded-lg p-8 text-center text-gray-600">
        No changes detected
      </div>
    )
  }

  return (
    <div className="space-y-4">
      {diff.fileDiffs.map((fileDiff) => {
        const isExpanded = expandedFiles.has(fileDiff.relativePath)
        return (
          <div key={fileDiff.relativePath} className="border rounded-lg overflow-hidden">
            <button
              onClick={() => toggleFile(fileDiff.relativePath)}
              className="w-full px-4 py-3 bg-gray-50 hover:bg-gray-100 flex items-center justify-between text-left"
            >
              <div className="flex items-center gap-3">
                <FileCode className="w-5 h-5 text-gray-600" />
                <span className="font-mono text-sm">{fileDiff.relativePath}</span>
              </div>
              <div className="flex items-center gap-4">
                <div className="text-sm">
                  <span className="text-green-600">+{fileDiff.additions}</span>
                  {' '}
                  <span className="text-red-600">-{fileDiff.deletions}</span>
                </div>
                <span className="text-xs text-gray-500">
                  {isExpanded ? '▼' : '▶'}
                </span>
              </div>
            </button>
            
            {isExpanded && (
              <div className="border-t">
                <div className="overflow-x-auto">
                  <table className="w-full text-sm font-mono">
                    <tbody>
                      {fileDiff.lines.map((line, idx) => (
                        <tr
                          key={idx}
                          className={`${getLineColor(line.type)} hover:bg-opacity-80`}
                        >
                          <td className="px-3 py-1 text-right text-gray-500 border-r w-16">
                            {line.originalLineNumber > 0 ? line.originalLineNumber : ''}
                          </td>
                          <td className="px-3 py-1 text-right text-gray-500 border-r w-16">
                            {line.transformedLineNumber > 0 ? line.transformedLineNumber : ''}
                          </td>
                          <td className="px-3 py-1 w-8 text-center">
                            {getLineIcon(line.type)}
                          </td>
                          <td className="px-3 py-1 whitespace-pre-wrap break-words">
                            {line.content || '\u00A0'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>
        )
      })}
    </div>
  )
}

