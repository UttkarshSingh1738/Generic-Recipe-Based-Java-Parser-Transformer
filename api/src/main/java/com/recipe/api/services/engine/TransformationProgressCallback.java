package com.recipe.api.services.engine;

public interface TransformationProgressCallback {
    void onFileProcessed(String fileName, boolean changed, int totalProcessed, int totalFiles);
    void onRecipeApplied(String recipeName, String fileName, int matches);
    void onError(String fileName, String error);
    void onComplete(int totalFiles, int filesTransformed, int filesFailed);
}

