package com.recipe.api.config;

import org.springframework.context.annotation.Configuration;

/**
 * Storage configuration - storage services are automatically loaded
 * based on storage.type property via @ConditionalOnProperty annotations
 */
@Configuration
public class StorageConfig {
    // Storage services are configured via @ConditionalOnProperty
    // in their respective service classes
}

