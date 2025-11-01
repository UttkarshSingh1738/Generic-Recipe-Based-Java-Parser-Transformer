import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: 'CodeForge - Enterprise Java Code Transformation',
  description: 'Transform Java codebases with AI-powered recipes',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en" className="dark">
      <body className={`${inter.className} dark bg-gray-950 text-gray-100`}>{children}</body>
    </html>
  )
}

