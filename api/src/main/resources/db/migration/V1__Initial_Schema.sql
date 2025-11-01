-- Initial Database Schema
-- This file documents the database schema for reference
-- JPA will auto-generate the schema from entities with ddl-auto: update

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    full_name VARCHAR(255),
    role VARCHAR(50) DEFAULT 'USER',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Recipes Table
CREATE TABLE IF NOT EXISTS recipes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1000),
    recipe_json TEXT,
    version VARCHAR(50),
    author VARCHAR(255),
    tags VARCHAR(500),
    category VARCHAR(100),
    is_public BOOLEAN DEFAULT false,
    created_by_user_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_recipes_name ON recipes(name);
CREATE INDEX IF NOT EXISTS idx_recipes_category ON recipes(category);
CREATE INDEX IF NOT EXISTS idx_recipes_is_public ON recipes(is_public);

-- Projects Table
CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    storage_path VARCHAR(500),
    source_path VARCHAR(500),
    file_count INTEGER,
    created_by_user_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_projects_created_by ON projects(created_by_user_id);

-- Transformation Jobs Table
CREATE TABLE IF NOT EXISTS transformation_jobs (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    recipe_names VARCHAR(2000),
    status VARCHAR(50) DEFAULT 'PENDING',
    output_path VARCHAR(500),
    log_path VARCHAR(500),
    files_transformed INTEGER DEFAULT 0,
    files_failed INTEGER DEFAULT 0,
    error_message TEXT,
    created_by_user_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_jobs_project_id ON transformation_jobs(project_id);
CREATE INDEX IF NOT EXISTS idx_jobs_status ON transformation_jobs(status);
CREATE INDEX IF NOT EXISTS idx_jobs_created_at ON transformation_jobs(created_at DESC);

